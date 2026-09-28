package com.stockpulse.service;

import com.stockpulse.entity.Product;
import com.stockpulse.enums.TriggerReason;
import org.springframework.stereotype.Component;

@Component
public class DemandSpikePromptBuilder {

    public String buildPrompt(Product product, TriggerReason triggerReason, double categoryAvgVelocity) {
        double vel = product.getDemandVelocity() != null ? product.getDemandVelocity() :
                (product.getSalesVelocity() != null ? product.getSalesVelocity() : 0.0);
        int views = product.getViewsCount() != null ? product.getViewsCount() : 0;

        return String.format("""
            You are an AI Commerce Engine evaluating a demand velocity spike situation.
            Product Details:
            - Name: %s (SKU: %s)
            - Category: %s
            - Current Price: $%.2f
            - Base Price: $%.2f, Cost Price: $%.2f
            - Min Price: $%.2f, Max Price: $%.2f
            - Sales/Demand Velocity: %.2f units/day (Category Average: %.2f units/day)
            - Live Page Views: %d
            - Trigger Reason: %s

            Task:
            Provide dynamic pricing adjustment and inventory reorder recommendation in strictly formatted JSON:
            {
              "suggestedPrice": number,
              "direction": "INCREASE" | "DECREASE" | "MAINTAIN",
              "confidenceScore": number (0.0 to 1.0),
              "suggestedReorderQuantity": number,
              "reasoning": "text explanation"
            }
            """,
                product.getName(), product.getSku(), product.getCategory(),
                product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice(),
                product.getBasePrice() != null ? product.getBasePrice() : java.math.BigDecimal.ZERO,
                product.getCostPrice() != null ? product.getCostPrice() : java.math.BigDecimal.ZERO,
                product.getMinPrice() != null ? product.getMinPrice() : java.math.BigDecimal.ZERO,
                product.getMaxPrice() != null ? product.getMaxPrice() : java.math.BigDecimal.valueOf(9999),
                vel, categoryAvgVelocity, views,
                triggerReason != null ? triggerReason.name() : "DEMAND_SURGE"
        );
    }
}
