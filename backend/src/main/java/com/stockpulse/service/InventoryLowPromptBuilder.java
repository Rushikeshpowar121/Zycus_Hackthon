package com.stockpulse.service;

import com.stockpulse.entity.Product;
import com.stockpulse.enums.TriggerReason;
import org.springframework.stereotype.Component;

@Component
public class InventoryLowPromptBuilder {

    public String buildPrompt(Product product, TriggerReason triggerReason) {
        int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int threshold = product.getReorderThreshold() != null ? product.getReorderThreshold() :
                (product.getReorderPoint() != null ? product.getReorderPoint() : 10);
        int maxStock = product.getMaxStockLimit() != null ? product.getMaxStockLimit() : 100;

        return String.format("""
            You are an AI Commerce Engine evaluating a low stock inventory situation.
            Product Details:
            - Name: %s (SKU: %s)
            - Category: %s
            - Current Price: $%.2f
            - Cost Price: $%.2f
            - Min Price: $%.2f, Max Price: $%.2f
            - Current Stock: %d (Reorder Threshold: %d, Max Stock Limit: %d)
            - Trigger Reason: %s

            Task:
            Provide dynamic pricing adjustment and reorder recommendation in strictly formatted JSON:
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
                product.getCostPrice() != null ? product.getCostPrice() : java.math.BigDecimal.ZERO,
                product.getMinPrice() != null ? product.getMinPrice() : java.math.BigDecimal.ZERO,
                product.getMaxPrice() != null ? product.getMaxPrice() : java.math.BigDecimal.valueOf(9999),
                stock, threshold, maxStock,
                triggerReason != null ? triggerReason.name() : "LOW_STOCK"
        );
    }
}
