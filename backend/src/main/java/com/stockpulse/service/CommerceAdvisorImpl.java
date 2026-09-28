package com.stockpulse.service;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.dto.RecommendationBundle;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.Direction;
import com.stockpulse.enums.SuggestionStatus;
import com.stockpulse.enums.TriggerReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommerceAdvisorImpl implements CommerceAdvisor {

    private final PricingEngineService pricingEngineService;

    @Override
    public RecommendationBundle recommend(Product product, TriggerReason triggerReason) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // Calculate pricing recommendation using existing pricing engine without mutating it
        PricingCalculationResultDTO priceResult = pricingEngineService.calculateRecommendation(
                product.getId() != null ? product.getId() : 1L, null);

        BigDecimal oldPrice = product.getCurrentPrice();
        BigDecimal suggestedPrice = priceResult.getRecommendedPrice();

        Direction direction = Direction.MAINTAIN;
        if (suggestedPrice.compareTo(oldPrice) > 0) {
            direction = Direction.INCREASE;
        } else if (suggestedPrice.compareTo(oldPrice) < 0) {
            direction = Direction.DECREASE;
        }

        PricingSuggestion pricingSuggestion = PricingSuggestion.builder()
                .productId(product.getId())
                .oldPrice(oldPrice)
                .suggestedPrice(suggestedPrice)
                .direction(direction)
                .confidenceScore(priceResult.getConfidenceScore())
                .reason(priceResult.getReasoning())
                .status(SuggestionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        ReorderSuggestion reorderSuggestion = null;
        int threshold = product.getReorderThreshold() != null ? product.getReorderThreshold() : 
                (product.getReorderPoint() != null ? product.getReorderPoint() : 10);

        if (product.getStockQuantity() != null && product.getStockQuantity() <= threshold) {
            int maxLimit = product.getMaxStockLimit() != null ? product.getMaxStockLimit() : (threshold * 3);
            int neededQuantity = Math.max(10, maxLimit - product.getStockQuantity());

            reorderSuggestion = ReorderSuggestion.builder()
                    .productId(product.getId())
                    .supplierId(product.getSupplierId() != null ? product.getSupplierId() : 101L)
                    .suggestedQuantity(neededQuantity)
                    .reason(String.format("Stock level (%d) is at or below reorder threshold (%d). Recommended restock.", 
                            product.getStockQuantity(), threshold))
                    .status(SuggestionStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        String summary = String.format("Commerce Advisor generated pricing (%s) & reorder recommendation triggered by %s.",
                direction, triggerReason != null ? triggerReason : TriggerReason.SCHEDULED_ANALYSIS);

        return RecommendationBundle.builder()
                .product(product)
                .triggerReason(triggerReason != null ? triggerReason : TriggerReason.SCHEDULED_ANALYSIS)
                .pricingSuggestion(pricingSuggestion)
                .reorderSuggestion(reorderSuggestion)
                .summary(summary)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
