package com.stockpulse.dto;

import com.stockpulse.enums.PricingStrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceAuditDTO {
    private Long id;
    private Long productId;
    private String productSku;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private Double priceChangePercent;
    private PricingStrategyType strategyUsed;
    private String reason;
    private LocalDateTime timestamp;
}
