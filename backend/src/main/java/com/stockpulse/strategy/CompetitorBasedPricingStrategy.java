package com.stockpulse.strategy;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.PricingStrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CompetitorBasedPricingStrategy implements PricingStrategy {

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.COMPETITOR_BASED;
    }

    @Override
    public PricingCalculationResultDTO calculatePrice(Product product, DynamicPricingRule rule) {
        BigDecimal competitorIndex = product.getCompetitorPriceIndex();
        StringBuilder reason = new StringBuilder("Competitor-Based Strategy: ");

        if (competitorIndex == null || competitorIndex.compareTo(BigDecimal.ZERO) <= 0) {
            return PricingCalculationResultDTO.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .productName(product.getName())
                    .currentPrice(product.getCurrentPrice())
                    .recommendedPrice(product.getCurrentPrice())
                    .priceChangePercent(0.0)
                    .strategyType(getType())
                    .confidenceScore(0.50)
                    .reasoning("No valid competitor price index available. Retained current price.")
                    .autoApplied(false)
                    .build();
        }

        double targetMarginOffset = (rule != null && rule.getCompetitorMarginTarget() != null) 
                ? rule.getCompetitorMarginTarget() : -0.02; // default undercut by 2%

        double targetMultiplier = 1.0 + targetMarginOffset;
        BigDecimal targetPrice = competitorIndex.multiply(BigDecimal.valueOf(targetMultiplier)).setScale(2, RoundingMode.HALF_UP);

        reason.append(String.format("Competitor benchmark index is $%s. Target positioning ratio is %.2f.", competitorIndex, targetMultiplier));

        // Safeguard minimum profit margin above cost price (e.g., at least cost + 10%)
        BigDecimal minCostMarginPrice = product.getCostPrice().multiply(BigDecimal.valueOf(1.10)).setScale(2, RoundingMode.HALF_UP);
        if (targetPrice.compareTo(minCostMarginPrice) < 0) {
            targetPrice = minCostMarginPrice;
            reason.append(String.format(" Protected margin: adjusted to cost + 10%% minimum ($%s).", minCostMarginPrice));
        }

        // Clamp to min / max bounds
        if (targetPrice.compareTo(product.getMinPrice()) < 0) {
            targetPrice = product.getMinPrice();
            reason.append(String.format(" Clamped to min bounds ($%s).", product.getMinPrice()));
        } else if (targetPrice.compareTo(product.getMaxPrice()) > 0) {
            targetPrice = product.getMaxPrice();
            reason.append(String.format(" Clamped to max bounds ($%s).", product.getMaxPrice()));
        }

        double changePercent = targetPrice.subtract(product.getCurrentPrice())
                .divide(product.getCurrentPrice(), 4, RoundingMode.HALF_UP)
                .doubleValue() * 100.0;

        return PricingCalculationResultDTO.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .productName(product.getName())
                .currentPrice(product.getCurrentPrice())
                .recommendedPrice(targetPrice)
                .priceChangePercent(Math.round(changePercent * 100.0) / 100.0)
                .strategyType(getType())
                .confidenceScore(0.89)
                .reasoning(reason.toString())
                .autoApplied(false)
                .build();
    }
}
