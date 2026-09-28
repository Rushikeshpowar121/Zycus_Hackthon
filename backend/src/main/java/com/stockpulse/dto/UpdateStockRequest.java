package com.stockpulse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStockRequest {

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange; // Positive for restock, negative for sale/reduction

    @NotNull(message = "Action type is required (STOCK_RESTOCK, STOCK_SALE, STOCK_ADJUSTMENT)")
    private String action;

    private String notes;
}
