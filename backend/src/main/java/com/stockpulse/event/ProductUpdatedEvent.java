package com.stockpulse.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdatedEvent {
    private Long productId;
    private String sku;
    private Integer previousStock;
    private Integer newStock;
    private String eventType; // e.g. "STOCK_CHANGE", "ORDER_CREATED"
    private LocalDateTime timestamp;
}
