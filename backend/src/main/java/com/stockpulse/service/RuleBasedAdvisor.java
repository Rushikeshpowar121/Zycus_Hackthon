package com.stockpulse.service;

import com.stockpulse.dto.RecommendationBundle;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.Direction;
import com.stockpulse.enums.SuggestionStatus;
import com.stockpulse.enums.TriggerReason;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service("ruleBasedAdvisor")
@RequiredArgsConstructor
public class RuleBasedAdvisor implements CommerceAdvisor {

    private final ProductRepository productRepository;

    @Override
    public RecommendationBundle recommend(Product product, TriggerReason triggerReason) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        int stockLevel = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int threshold = product.getReorderThreshold() != null ? product.getReorderThreshold() :
                (product.getReorderPoint() != null ? product.getReorderPoint() : 10);

        double demandVel = product.getDemandVelocity() != null ? product.getDemandVelocity() :
                (product.getSalesVelocity() != null ? product.getSalesVelocity() : 0.0);

        // 1. Calculate Category Average Demand Velocity
        double categoryAvgVelocity = 0.0;
        if (product.getCategory() != null) {
            List<Product> categoryProducts = productRepository.findByCategory(product.getCategory());
            if (categoryProducts != null && !categoryProducts.isEmpty()) {
                double sum = 0.0;
                for (Product p : categoryProducts) {
                    double pVel = p.getDemandVelocity() != null ? p.getDemandVelocity() :
                            (p.getSalesVelocity() != null ? p.getSalesVelocity() : 0.0);
                    sum += pVel;
                }
                categoryAvgVelocity = sum / categoryProducts.size();
            }
        }

        // 2. Evaluate Rule Conditions for Pricing Adjustment
        double priceIncreaseMultiplier = 0.0;
        StringBuilder reasoning = new StringBuilder("Rule-Based Advisor Analysis: ");

        boolean lowStockCondition = stockLevel < threshold;
        boolean highDemandCondition = categoryAvgVelocity > 0 && demandVel > (categoryAvgVelocity * 2.0);

        if (lowStockCondition) {
            priceIncreaseMultiplier += 0.10; // +10% price increase when stock < reorderThreshold
            reasoning.append(String.format("Stock level (%d) < Reorder Threshold (%d): Applied +10%% price increase. ", stockLevel, threshold));
        }

        if (highDemandCondition) {
            priceIncreaseMultiplier += 0.05; // +5% price increase when demandVelocity > category avg * 2
            reasoning.append(String.format("Demand velocity (%.2f) > 2x Category Avg (%.2f): Applied +5%% price surge. ", demandVel, categoryAvgVelocity * 2.0));
        }

        if (!lowStockCondition && !highDemandCondition) {
            reasoning.append("No price increase rules triggered. Maintained current price.");
        }

        BigDecimal currentPrice = product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice();
        BigDecimal targetPrice = currentPrice.multiply(BigDecimal.valueOf(1.0 + priceIncreaseMultiplier)).setScale(2, RoundingMode.HALF_UP);

        // Clamp within product bounds if minPrice/maxPrice are set
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

        TriggerReason finalTrigger = triggerReason != null ? triggerReason : TriggerReason.SCHEDULED_ANALYSIS;

        PricingSuggestion pricingSuggestion = PricingSuggestion.builder()
                .productId(product.getId())
                .oldPrice(currentPrice)
                .suggestedPrice(targetPrice)
                .direction(direction)
                .confidenceScore(0.95)
                .reason(reasoning.toString().trim())
                .status(SuggestionStatus.PENDING)
                .triggerReason(finalTrigger)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. Generate Reorder Quantity: (reorderThreshold * 3) - stockLevel
        ReorderSuggestion reorderSuggestion = null;
        if (stockLevel < threshold) {
            int calculatedReorderQty = (threshold * 3) - stockLevel;
            int finalReorderQty = Math.max(1, calculatedReorderQty);

            reorderSuggestion = ReorderSuggestion.builder()
                    .productId(product.getId())
                    .supplierId(product.getSupplierId() != null ? product.getSupplierId() : 101L)
                    .suggestedQuantity(finalReorderQty)
                    .suggestedLeadTimeDays(7)
                    .confidenceScore(0.92)
                    .triggerReason(finalTrigger)
                    .reason(String.format("Rule Triggered: Stock level (%d) < Threshold (%d). Formula (Threshold * 3 - Stock) = %d units.",
                            stockLevel, threshold, finalReorderQty))
                    .status(SuggestionStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        String summary = String.format("RuleBasedAdvisor evaluated %s (Trigger: %s) -> Pricing Direction: %s, Reorder Needed: %s.",
                product.getSku(), finalTrigger, direction, reorderSuggestion != null ? "YES (" + reorderSuggestion.getSuggestedQuantity() + " units)" : "NO");

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
