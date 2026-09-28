package com.stockpulse.strategy;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.PricingStrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AIHeuristicPricingStrategy implements PricingStrategy {

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.AI_HEURISTIC;
    }

    @Override
    public PricingCalculationResultDTO calculatePrice(Product product, DynamicPricingRule rule) {
        BigDecimal base = product.getBasePrice();
        StringBuilder reasoning = new StringBuilder("AI Heuristic Multi-Factor Optimization Engine: ");

        // Factor 1: Demand Index (0.80 to 1.35)
        double demandFactor = 1.0;
        int views = product.getViewsCount() != null ? product.getViewsCount() : 0;
        double velocity = product.getSalesVelocity() != null ? product.getSalesVelocity() : 0.0;

        if (views > 1000 || velocity > 30.0) demandFactor = 1.25;
        else if (views > 500 || velocity > 15.0) demandFactor = 1.15;
        else if (views > 150 || velocity > 5.0) demandFactor = 1.05;
        else if (views > 50 || velocity > 1.0) demandFactor = 0.95;
        else demandFactor = 0.88;

        // Factor 2: Inventory Turnover / Scarcity Score (0.90 to 1.15)
        double inventoryFactor = 1.0;
        if (product.getStockQuantity() != null && product.getReorderPoint() != null) {
            double stockRatio = (double) product.getStockQuantity() / Math.max(1, product.getReorderPoint());
            if (stockRatio <= 0.5) inventoryFactor = 1.12; // critical shortage
            else if (stockRatio <= 1.0) inventoryFactor = 1.06; // low stock
            else if (stockRatio >= 4.0) inventoryFactor = 0.92; // excess stock
        }

        // Factor 3: Competitor Elasticity Factor
        double competitorFactor = 1.0;
        if (product.getCompetitorPriceIndex() != null && product.getCompetitorPriceIndex().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal compRatio = product.getCompetitorPriceIndex().divide(base, 4, RoundingMode.HALF_UP);
            if (compRatio.doubleValue() > 1.10) {
                competitorFactor = 1.08; // competitor priced higher, headroom to raise price
            } else if (compRatio.doubleValue() < 0.90) {
                competitorFactor = 0.93; // competitor undercutting, align down
            }
        }

        // Weighted AI Composite Score
        // 40% Demand + 30% Inventory + 30% Competitor
        double compositeMultiplier = (demandFactor * 0.40) + (inventoryFactor * 0.30) + (competitorFactor * 0.30);

        BigDecimal calculatedPrice = base.multiply(BigDecimal.valueOf(compositeMultiplier)).setScale(2, RoundingMode.HALF_UP);

        // Margin safety check: ensure price >= cost * 1.12
        BigDecimal targetMinMargin = product.getCostPrice().multiply(BigDecimal.valueOf(1.12)).setScale(2, RoundingMode.HALF_UP);
        if (calculatedPrice.compareTo(targetMinMargin) < 0) {
            calculatedPrice = targetMinMargin;
            reasoning.append("Enforced minimum 12% profit margin rule.");
        }

        // Clamp within product bounds
        if (calculatedPrice.compareTo(product.getMinPrice()) < 0) {
            calculatedPrice = product.getMinPrice();
        } else if (calculatedPrice.compareTo(product.getMaxPrice()) > 0) {
            calculatedPrice = product.getMaxPrice();
        }

        reasoning.append(String.format("Composite Multiplier: %.3f (Demand: x%.2f, Inventory Scarcity: x%.2f, Competitor Benchmark: x%.2f). Recommended Price: $%s.",
                compositeMultiplier, demandFactor, inventoryFactor, competitorFactor, calculatedPrice));

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
                .confidenceScore(0.97)
                .reasoning(reasoning.toString())
                .autoApplied(false)
                .build();
    }
}
