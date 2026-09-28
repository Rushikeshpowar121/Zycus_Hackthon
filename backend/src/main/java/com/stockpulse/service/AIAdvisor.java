package com.stockpulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpulse.dto.RecommendationBundle;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.Direction;
import com.stockpulse.enums.SuggestionStatus;
import com.stockpulse.enums.TriggerReason;
import com.stockpulse.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("aiAdvisor")
@Slf4j
public class AIAdvisor implements CommerceAdvisor {

    private final InventoryLowPromptBuilder inventoryLowPromptBuilder;
    private final DemandSpikePromptBuilder demandSpikePromptBuilder;
    private final AIValidationService aiValidationService;
    private final CommerceAdvisor ruleBasedAdvisor;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    public AIAdvisor(
            InventoryLowPromptBuilder inventoryLowPromptBuilder,
            DemandSpikePromptBuilder demandSpikePromptBuilder,
            AIValidationService aiValidationService,
            @Qualifier("ruleBasedAdvisor") CommerceAdvisor ruleBasedAdvisor,
            ProductRepository productRepository) {
        this.inventoryLowPromptBuilder = inventoryLowPromptBuilder;
        this.demandSpikePromptBuilder = demandSpikePromptBuilder;
        this.aiValidationService = aiValidationService;
        this.ruleBasedAdvisor = ruleBasedAdvisor;
        this.productRepository = productRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public RecommendationBundle recommend(Product product, TriggerReason triggerReason) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        try {
            log.info("Attempting Gemini AI Advisor Recommendation for SKU: {} (Trigger: {})",
                    product.getSku(), triggerReason);

            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                throw new IllegalStateException("Gemini API key is unconfigured");
            }

            // 1. Select appropriate prompt builder based on scenario
            String promptText;
            TriggerReason reason = triggerReason != null ? triggerReason : TriggerReason.SCHEDULED_ANALYSIS;
            
            if (reason == TriggerReason.LOW_STOCK || (product.getStockQuantity() != null && 
                    product.getStockQuantity() <= (product.getReorderThreshold() != null ? product.getReorderThreshold() : 10))) {
                promptText = inventoryLowPromptBuilder.buildPrompt(product, reason);
            } else {
                double avgCategoryVel = getCategoryAverageVelocity(product.getCategory());
                promptText = demandSpikePromptBuilder.buildPrompt(product, reason, avgCategoryVel);
            }

            // 2. Call Gemini API
            String aiRawResponse = callGeminiApi(promptText);

            // 3. Parse Gemini Response JSON
            RecommendationBundle bundle = parseGeminiResponse(product, reason, aiRawResponse);

            // 4. Validate output using AIValidationService
            if (aiValidationService.validate(bundle)) {
                log.info("Gemini AI Advisor recommendation successfully generated and validated for SKU: {}", product.getSku());
                return bundle;
            } else {
                log.warn("Gemini AI generated response failed domain validation rules. Triggering fallback.");
                throw new IllegalStateException("AI recommendation validation failed");
            }

        } catch (Exception e) {
            log.warn("Gemini AI Advisor failed or unconfigured ({}), executing fallback to RuleBasedAdvisor.", e.getMessage());
            return ruleBasedAdvisor.recommend(product, triggerReason);
        }
    }

    private double getCategoryAverageVelocity(String category) {
        if (category == null) return 0.0;
        List<Product> products = productRepository.findByCategory(category);
        if (products == null || products.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Product p : products) {
            double v = p.getDemandVelocity() != null ? p.getDemandVelocity() : (p.getSalesVelocity() != null ? p.getSalesVelocity() : 0.0);
            sum += v;
        }
        return sum / products.size();
    }

    private String callGeminiApi(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("parts", Collections.singletonList(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(contentObj));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Gemini API HTTP Error status: " + response.getStatusCode());
        }
    }

    private RecommendationBundle parseGeminiResponse(Product product, TriggerReason triggerReason, String jsonResponse) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
        
        String responseText = textNode.asText();
        // Clean markdown codeblocks if present (e.g. ```json ... ```)
        if (responseText.contains("```")) {
            responseText = responseText.replaceAll("```json", "").replaceAll("```", "").trim();
        }

        JsonNode parsedAi = objectMapper.readTree(responseText);

        double suggestedPriceVal = parsedAi.path("suggestedPrice").asDouble(product.getCurrentPrice().doubleValue());
        String dirStr = parsedAi.path("direction").asText("MAINTAIN");
        double confidence = parsedAi.path("confidenceScore").asDouble(0.90);
        int reorderQty = parsedAi.path("suggestedReorderQuantity").asInt(0);
        String reasoning = parsedAi.path("reasoning").asText("Gemini AI Optimization Analysis");

        Direction direction;
        try {
            direction = Direction.valueOf(dirStr.toUpperCase());
        } catch (Exception ex) {
            direction = Direction.MAINTAIN;
        }

        PricingSuggestion pricingSuggestion = PricingSuggestion.builder()
                .productId(product.getId())
                .oldPrice(product.getCurrentPrice())
                .suggestedPrice(BigDecimal.valueOf(suggestedPriceVal))
                .direction(direction)
                .confidenceScore(confidence)
                .reason("Gemini AI: " + reasoning)
                .triggerReason(triggerReason)
                .status(SuggestionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        ReorderSuggestion reorderSuggestion = null;
        if (reorderQty > 0) {
            reorderSuggestion = ReorderSuggestion.builder()
                    .productId(product.getId())
                    .supplierId(product.getSupplierId() != null ? product.getSupplierId() : 101L)
                    .suggestedQuantity(reorderQty)
                    .suggestedLeadTimeDays(7)
                    .confidenceScore(confidence)
                    .triggerReason(triggerReason)
                    .reason("Gemini AI: Reorder recommended based on inventory analysis.")
                    .status(SuggestionStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        return RecommendationBundle.builder()
                .product(product)
                .triggerReason(triggerReason)
                .pricingSuggestion(pricingSuggestion)
                .reorderSuggestion(reorderSuggestion)
                .summary("Gemini AI Advisor evaluated product " + product.getSku())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
