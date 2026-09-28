package com.stockpulse.strategy;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.PricingStrategyType;

public interface PricingStrategy {
    PricingStrategyType getType();
    PricingCalculationResultDTO calculatePrice(Product product, DynamicPricingRule rule);
}
