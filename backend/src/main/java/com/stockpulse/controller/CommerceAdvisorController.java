package com.stockpulse.controller;

import com.stockpulse.dto.RecommendationBundle;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.entity.SystemConfiguration;
import com.stockpulse.enums.AdvisorType;
import com.stockpulse.enums.TriggerReason;
import com.stockpulse.repository.PricingSuggestionRepository;
import com.stockpulse.repository.ReorderSuggestionRepository;
import com.stockpulse.service.AdvisorRegistry;
import com.stockpulse.service.CommerceAdvisor;
import com.stockpulse.service.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/advisor")
public class CommerceAdvisorController {

    private final AdvisorRegistry advisorRegistry;
    private final CommerceAdvisor defaultCommerceAdvisor;
    private final CommerceAdvisor ruleBasedAdvisor;
    private final CommerceAdvisor aiAdvisor;
    private final CommerceAdvisor competitorAwareAdvisor;
    private final ProductService productService;
    private final PricingSuggestionRepository pricingSuggestionRepository;
    private final ReorderSuggestionRepository reorderSuggestionRepository;

    public CommerceAdvisorController(
            AdvisorRegistry advisorRegistry,
            @Qualifier("commerceAdvisorImpl") CommerceAdvisor defaultCommerceAdvisor,
            @Qualifier("ruleBasedAdvisor") CommerceAdvisor ruleBasedAdvisor,
            @Qualifier("aiAdvisor") CommerceAdvisor aiAdvisor,
            @Qualifier("competitorAwareAdvisor") CommerceAdvisor competitorAwareAdvisor,
            ProductService productService,
            PricingSuggestionRepository pricingSuggestionRepository,
            ReorderSuggestionRepository reorderSuggestionRepository) {
        this.advisorRegistry = advisorRegistry;
        this.defaultCommerceAdvisor = defaultCommerceAdvisor;
        this.ruleBasedAdvisor = ruleBasedAdvisor;
        this.aiAdvisor = aiAdvisor;
        this.competitorAwareAdvisor = competitorAwareAdvisor;
        this.productService = productService;
        this.pricingSuggestionRepository = pricingSuggestionRepository;
        this.reorderSuggestionRepository = reorderSuggestionRepository;
    }

    @GetMapping("/recommend/{productId}")
    public ResponseEntity<RecommendationBundle> getRecommendation(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "SCHEDULED_ANALYSIS") TriggerReason triggerReason) {
        Product product = productService.getProductEntityById(productId);
        return ResponseEntity.ok(advisorRegistry.getActiveAdvisor().recommend(product, triggerReason));
    }

    @GetMapping("/rule-recommend/{productId}")
    public ResponseEntity<RecommendationBundle> getRuleRecommendation(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "SCHEDULED_ANALYSIS") TriggerReason triggerReason) {
        Product product = productService.getProductEntityById(productId);
        return ResponseEntity.ok(ruleBasedAdvisor.recommend(product, triggerReason));
    }

    @GetMapping("/ai-recommend/{productId}")
    public ResponseEntity<RecommendationBundle> getAiRecommendation(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "SCHEDULED_ANALYSIS") TriggerReason triggerReason) {
        Product product = productService.getProductEntityById(productId);
        return ResponseEntity.ok(aiAdvisor.recommend(product, triggerReason));
    }

    @GetMapping("/competitor-recommend/{productId}")
    public ResponseEntity<RecommendationBundle> getCompetitorRecommendation(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "COMPETITOR_PRICE_CHANGE") TriggerReason triggerReason) {
        Product product = productService.getProductEntityById(productId);
        return ResponseEntity.ok(competitorAwareAdvisor.recommend(product, triggerReason));
    }

    @GetMapping("/pricing-suggestions")
    public ResponseEntity<List<PricingSuggestion>> getPricingSuggestions() {
        return ResponseEntity.ok(pricingSuggestionRepository.findAll());
    }

    @GetMapping("/reorder-suggestions")
    public ResponseEntity<List<ReorderSuggestion>> getReorderSuggestions() {
        return ResponseEntity.ok(reorderSuggestionRepository.findAll());
    }

    @GetMapping("/config")
    public ResponseEntity<AdvisorType> getActiveAdvisorConfig() {
        return ResponseEntity.ok(advisorRegistry.getActiveAdvisorType());
    }

    @PutMapping("/config")
    public ResponseEntity<SystemConfiguration> setActiveAdvisorConfig(@RequestParam AdvisorType type) {
        return ResponseEntity.ok(advisorRegistry.setActiveAdvisorType(type));
    }
}
