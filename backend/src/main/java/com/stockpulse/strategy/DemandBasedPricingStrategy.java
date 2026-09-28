package com.stockpulse.strategy;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.DemandLevel;
import com.stockpulse.enums.PricingStrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DemandBasedPricingStrategy implements PricingStrategy {

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.DEMAND_BASED;
    }

    @Override
    public PricingCalculationResultDTO calculatePrice(Product product, DynamicPricingRule rule) {
        BigDecimal base = product.getBasePrice();
        double multiplier = 1.0;
        StringBuilder reason = new StringBuilder("Demand-Based Strategy: ");

        DemandLevel demand = product.getDemandLevel();
        double configSurge = (rule != null && rule.getSurgeMultiplier() != null) ? rule.getSurgeMultiplier() : 1.15;

        switch (demand) {
            case SURGE:
                multiplier = configSurge;
                reason.append(String.format("Surge demand detected (Views: %d, Velocity: %.1f). Applied surge multiplier x%.2f.", 
                        product.getViewsCount(), product.getSalesVelocity(), multiplier));
                break;
            case HIGH:
                multiplier = 1.00 + ((configSurge - 1.00) * 0.5);
                reason.append(String.format("High demand detected. Applied demand boost x%.2f.", multiplier));
                break;
            case NORMAL:
                multiplier = 1.00;
                reason.append("Demand is normal. Price aligned with base price.");
                break;
            case LOW:
                multiplier = 0.95;
                reason.append("Low demand detected. Applied 5% demand stimulus discount.");
                break;
            case VERY_LOW:
                multiplier = 0.90;
                reason.append("Very low demand detected. Applied 10% clearance stimulus discount.");
                break;
        }

        // Adjust for stock scarcity
        if (product.getStockQuantity() != null && product.getReorderPoint() != null) {
            if (product.getStockQuantity() <= product.getReorderPoint() && product.getStockQuantity() > 0) {
                multiplier += 0.05; // scarcity premium
                reason.append(" + 5% Scarcity Premium (Stock <= Reorder Point).");
            }
        }

        BigDecimal calculatedPrice = base.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);

        // Clamp to min / max limits
        if (calculatedPrice.compareTo(product.getMinPrice()) < 0) {
            calculatedPrice = product.getMinPrice();
            reason.append(String.format(" Clamped to min threshold ($%s).", product.getMinPrice()));
        } else if (calculatedPrice.compareTo(product.getMaxPrice()) > 0) {
            calculatedPrice = product.getMaxPrice();
            reason.append(String.format(" Clamped to max threshold ($%s).", product.getMaxPrice()));
        }

        double changePercent = calculatedPrice.subtract(product.getCurrentPrice())
                .divide(product.getCurrentPrice(), 4, RoundingMode.HALF_UP)
                .doubleValue() * 100.0;

        return PricingCalculationResultDTO.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .productName(product.getName())
                .currentPrice(product.getCurrentPrice())
                .recommendedPrice(calculatedPrice)
                .priceChangePercent(Math.round(changePercent * 100.0) / 100.0)
                .strategyType(getType())
                .confidenceScore(0.92)
                .reasoning(reason.toString())
                .autoApplied(false)
                .build();
    }
}
