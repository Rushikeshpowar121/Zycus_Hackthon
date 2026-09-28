package com.stockpulse.service;

import com.stockpulse.dto.RecommendationBundle;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.Direction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AIValidationService {

    public boolean validate(RecommendationBundle bundle) {
        if (bundle == null || bundle.getProduct() == null) {
            return false;
        }

        Product product = bundle.getProduct();
        PricingSuggestion pricing = bundle.getPricingSuggestion();

        if (pricing != null) {
            BigDecimal price = pricing.getSuggestedPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // Must not violate minimum cost price
            if (product.getCostPrice() != null && price.compareTo(product.getCostPrice()) < 0) {
                return false;
            }

            // Must not exceed max price or drop below min price bounds
            if (product.getMinPrice() != null && price.compareTo(product.getMinPrice()) < 0) {
                return false;
            }
            if (product.getMaxPrice() != null && price.compareTo(product.getMaxPrice()) > 0) {
                return false;
            }

            // Direction check
            Direction dir = pricing.getDirection();
            if (dir == null) {
                return false;
            }
        }

        ReorderSuggestion reorder = bundle.getReorderSuggestion();
        if (reorder != null) {
            Integer qty = reorder.getSuggestedQuantity();
            if (qty == null || qty < 0) {
                return false;
            }
        }

        return true;
    }
}
