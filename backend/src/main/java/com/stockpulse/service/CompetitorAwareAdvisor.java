package com.stockpulse.service;

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
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service("competitorAwareAdvisor")
@RequiredArgsConstructor
public class CompetitorAwareAdvisor implements CommerceAdvisor {

    @Override
    public RecommendationBundle recommend(Product product, TriggerReason triggerReason) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        BigDecimal currentPrice = product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice();
        BigDecimal competitorIndex = product.getCompetitorPriceIndex();
        StringBuilder reasoning = new StringBuilder("Competitor-Aware Advisor Analysis: ");

        BigDecimal targetPrice = currentPrice;
        if (competitorIndex != null && competitorIndex.compareTo(BigDecimal.ZERO) > 0) {
            // Target price undercuts competitor by 2%
            targetPrice = competitorIndex.multiply(BigDecimal.valueOf(0.98)).setScale(2, RoundingMode.HALF_UP);
            reasoning.append(String.format("Competitor price is $%s. Target positioning set to 2%% undercut ($%s). ", competitorIndex, targetPrice));
        } else {
            reasoning.append("No competitor benchmark price available. Retained current price. ");
        }

        // Safeguard minimum profit margin above cost
        if (product.getCostPrice() != null) {
            BigDecimal minAllowed = product.getCostPrice().multiply(BigDecimal.valueOf(1.10)).setScale(2, RoundingMode.HALF_UP);
            if (targetPrice.compareTo(minAllowed) < 0) {
                targetPrice = minAllowed;
                reasoning.append(String.format("Margin protected: adjusted to cost + 10%% minimum ($%s). ", minAllowed));
            }
        }

        // Clamp to min/max bounds
        if (product.getMinPrice() != null && targetPrice.compareTo(product.getMinPrice()) < 0) {
            targetPrice = product.getMinPrice();
        } else if (product.getMaxPrice() != null && targetPrice.compareTo(product.getMaxPrice()) > 0) {
            targetPrice = product.getMaxPrice();
        }

        Direction direction = Direction.MAINTAIN;
        if (targetPrice.compareTo(currentPrice) > 0) {
            direction = Direction.INCREASE;
        } else if (targetPrice.compareTo(currentPrice) < 0) {
            direction = Direction.DECREASE;
        }

        PricingSuggestion pricingSuggestion = PricingSuggestion.builder()
                .productId(product.getId())
                .oldPrice(currentPrice)
                .suggestedPrice(targetPrice)
                .direction(direction)
                .confidenceScore(0.91)
                .reason(reasoning.toString().trim())
                .status(SuggestionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        ReorderSuggestion reorderSuggestion = null;
        int stockLevel = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int threshold = product.getReorderThreshold() != null ? product.getReorderThreshold() :
                (product.getReorderPoint() != null ? product.getReorderPoint() : 10);

        if (stockLevel <= threshold) {
            int maxLimit = product.getMaxStockLimit() != null ? product.getMaxStockLimit() : (threshold * 3);
            int neededQuantity = Math.max(10, maxLimit - stockLevel);

            reorderSuggestion = ReorderSuggestion.builder()
                    .productId(product.getId())
                    .supplierId(product.getSupplierId() != null ? product.getSupplierId() : 101L)
                    .suggestedQuantity(neededQuantity)
                    .reason(String.format("Competitor Advisor: Stock level (%d) <= threshold (%d). Recommended restock.", stockLevel, threshold))
                    .status(SuggestionStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        TriggerReason finalTrigger = triggerReason != null ? triggerReason : TriggerReason.COMPETITOR_PRICE_CHANGE;
        String summary = String.format("CompetitorAwareAdvisor evaluated %s (Trigger: %s) -> Target Price: $%s, Direction: %s.",
                product.getSku(), finalTrigger, targetPrice, direction);

        return RecommendationBundle.builder()
                .product(product)
                .triggerReason(finalTrigger)
                .pricingSuggestion(pricingSuggestion)
                .reorderSuggestion(reorderSuggestion)
                .summary(summary)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
