package com.stockpulse.dto;

import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.TriggerReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationBundle {
    private Product product;
    private TriggerReason triggerReason;
    private PricingSuggestion pricingSuggestion;
    private ReorderSuggestion reorderSuggestion;
    private String summary;
    private LocalDateTime timestamp;
}
