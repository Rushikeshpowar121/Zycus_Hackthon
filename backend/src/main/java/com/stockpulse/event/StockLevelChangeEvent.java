package com.stockpulse.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevelChangeEvent {
    private Long productId;
    private String sku;
    private Integer previousStock;
    private Integer newStock;
    private String action;
    private String notes;
}
