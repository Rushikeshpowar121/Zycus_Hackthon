package com.stockpulse.strategy;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.PricingStrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AgingInventoryPricingStrategy implements PricingStrategy {

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.AGING_INVENTORY;
    }

    @Override
    public PricingCalculationResultDTO calculatePrice(Product product, DynamicPricingRule rule) {
        BigDecimal base = product.getBasePrice();
        StringBuilder reason = new StringBuilder("Aging & Expiry Inventory Strategy: ");
        double maxDiscount = (rule != null && rule.getMaxDiscountPercent() != null) ? rule.getMaxDiscountPercent() : 0.30;
        double discountFactor = 0.0;

        // Perishable expiry calculation
        if (product.getDaysToExpiry() != null) {
            int days = product.getDaysToExpiry();
            if (days <= 3) {
                discountFactor = Math.min(0.35, maxDiscount);
                reason.append(String.format("Critical expiration imminent (%d days remaining). Applied maximum liquidation discount of %.0f%%.", days, discountFactor * 100));
            } else if (days <= 7) {
                discountFactor = Math.min(0.25, maxDiscount);
                reason.append(String.format("Expiry near (%d days remaining). Applied urgent discount of %.0f%%.", days, discountFactor * 100));
            } else if (days <= 14) {
                discountFactor = Math.min(0.15, maxDiscount);
                reason.append(String.format("Expiry approaching (%d days remaining). Applied decay discount of %.0f%%.", days, discountFactor * 100));
            }
        }

        // Non-perishable aging calculation
        if (discountFactor == 0.0 && product.getDaysInStock() != null) {
            int daysInStock = product.getDaysInStock();
            if (daysInStock > 90) {
                discountFactor = Math.min(0.25, maxDiscount);
                reason.append(String.format("Product in stock for %d days (>90 days). Applied slow-mover discount of %.0f%%.", daysInStock, discountFactor * 100));
            } else if (daysInStock > 60) {
                discountFactor = Math.min(0.15, maxDiscount);
                reason.append(String.format("Product in stock for %d days (>60 days). Applied aging discount of %.0f%%.", daysInStock, discountFactor * 100));
            } else if (daysInStock > 30) {
                discountFactor = Math.min(0.08, maxDiscount);
                reason.append(String.format("Product in stock for %d days (>30 days). Applied moderate aging discount of %.0f%%.", daysInStock, discountFactor * 100));
            } else {
                reason.append(String.format("Product stock age is fresh (%d days). No aging discount required.", daysInStock));
            }
        }

        BigDecimal calculatedPrice = base.multiply(BigDecimal.valueOf(1.0 - discountFactor)).setScale(2, RoundingMode.HALF_UP);

        // Safeguard cost threshold
        if (calculatedPrice.compareTo(product.getCostPrice()) < 0) {
            calculatedPrice = product.getCostPrice();
            reason.append(String.format(" Discount capped at cost price break-even ($%s).", product.getCostPrice()));
        }

        // Clamp to min price
        if (calculatedPrice.compareTo(product.getMinPrice()) < 0) {
            calculatedPrice = product.getMinPrice();
            reason.append(String.format(" Clamped to min price threshold ($%s).", product.getMinPrice()));
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
                .confidenceScore(0.95)
                .reasoning(reason.toString())
                .autoApplied(false)
                .build();
    }
}
