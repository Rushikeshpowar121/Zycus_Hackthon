package com.stockpulse.event;

import com.stockpulse.enums.PricingStrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicPriceUpdatedEvent {
    private Long productId;
    private String sku;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private PricingStrategyType strategyUsed;
    private String reason;
}
