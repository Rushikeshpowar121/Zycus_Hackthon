package com.stockpulse.service;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.dto.UpdateStrategyRequest;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.event.AsyncEventPublisher;
import com.stockpulse.event.DynamicPriceUpdatedEvent;
import com.stockpulse.repository.DynamicPricingRuleRepository;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.strategy.PricingStrategy;
import com.stockpulse.strategy.StrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingEngineServiceImpl implements PricingEngineService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final DynamicPricingRuleRepository ruleRepository;
    private final StrategyRegistry strategyRegistry;
    private final AsyncEventPublisher asyncEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PricingCalculationResultDTO calculateRecommendation(Long productId, PricingStrategyType overrideStrategy) {
        Product product = productService.getProductEntityById(productId);
        PricingStrategyType targetStrategy = overrideStrategy != null ? overrideStrategy : product.getActiveStrategyType();

        PricingStrategy strategy = strategyRegistry.getStrategy(targetStrategy);
        DynamicPricingRule rule = ruleRepository.findByStrategyType(targetStrategy).orElse(null);

        return strategy.calculatePrice(product, rule);
    }

    @Override
    @Transactional
    public ProductDTO applyPriceRecommendation(Long productId, PricingCalculationResultDTO recommendation) {
        Product product = productService.getProductEntityById(productId);
        BigDecimal oldPrice = product.getCurrentPrice();
        BigDecimal newPrice = recommendation.getRecommendedPrice();

        product.setCurrentPrice(newPrice);
        if (recommendation.getStrategyType() != null) {
            product.setActiveStrategyType(recommendation.getStrategyType());
        }

        Product saved = productRepository.save(product);

        // Publish Dynamic Price Update event asynchronously
        asyncEventPublisher.publishPriceUpdated(DynamicPriceUpdatedEvent.builder()
                .productId(saved.getId())
                .sku(saved.getSku())
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .strategyUsed(recommendation.getStrategyType() != null ? recommendation.getStrategyType() : saved.getActiveStrategyType())
                .reason(recommendation.getReasoning())
                .build());

        return productService.mapToDTO(saved);
    }

    @Override
    @Transactional
    public List<PricingCalculationResultDTO> batchRepriceAll() {
        List<Product> products = productRepository.findAll();
        List<PricingCalculationResultDTO> results = new ArrayList<>();

        for (Product product : products) {
            PricingCalculationResultDTO recommendation = calculateRecommendation(product.getId(), null);
            // Auto-apply if difference is greater than 1%
            if (Math.abs(recommendation.getPriceChangePercent()) >= 1.0) {
                applyPriceRecommendation(product.getId(), recommendation);
                recommendation.setAutoApplied(true);
            } else {
                recommendation.setAutoApplied(false);
            }
            results.add(recommendation);
        }

        return results;
    }

    @Override
    @Transactional
    public DynamicPricingRule updatePricingRule(UpdateStrategyRequest request) {
        DynamicPricingRule rule = ruleRepository.findByStrategyType(request.getStrategyType())
                .orElse(DynamicPricingRule.builder()
                        .strategyType(request.getStrategyType())
                        .active(true)
                        .build());

        if (request.getSurgeMultiplier() != null) rule.setSurgeMultiplier(request.getSurgeMultiplier());
        if (request.getDiscountRate() != null) rule.setDiscountRate(request.getDiscountRate());
        if (request.getCompetitorMarginTarget() != null) rule.setCompetitorMarginTarget(request.getCompetitorMarginTarget());
        if (request.getMaxDiscountPercent() != null) rule.setMaxDiscountPercent(request.getMaxDiscountPercent());

        return ruleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public DynamicPricingRule getPricingRule(PricingStrategyType type) {
        return ruleRepository.findByStrategyType(type)
                .orElseGet(() -> ruleRepository.save(DynamicPricingRule.builder()
                        .strategyType(type)
                        .surgeMultiplier(1.15)
                        .discountRate(0.10)
                        .competitorMarginTarget(-0.02)
                        .maxDiscountPercent(0.30)
                        .aiConfidenceScore(0.95)
                        .active(true)
                        .build()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DynamicPricingRule> getAllPricingRules() {
        return ruleRepository.findAll();
    }
}
