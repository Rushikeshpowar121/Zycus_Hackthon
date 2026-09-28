package com.stockpulse.service;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.dto.UpdateStrategyRequest;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.enums.PricingStrategyType;

import java.util.List;

public interface PricingEngineService {
    PricingCalculationResultDTO calculateRecommendation(Long productId, PricingStrategyType overrideStrategy);
    ProductDTO applyPriceRecommendation(Long productId, PricingCalculationResultDTO recommendation);
    List<PricingCalculationResultDTO> batchRepriceAll();
    DynamicPricingRule updatePricingRule(UpdateStrategyRequest request);
    DynamicPricingRule getPricingRule(PricingStrategyType type);
    List<DynamicPricingRule> getAllPricingRules();
}
