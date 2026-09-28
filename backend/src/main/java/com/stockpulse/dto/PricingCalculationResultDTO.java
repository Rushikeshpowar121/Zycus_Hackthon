package com.stockpulse.dto;

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
public class PricingCalculationResultDTO {
    private Long productId;
    private String sku;
    private String productName;
    private BigDecimal currentPrice;
    private BigDecimal recommendedPrice;
    private Double priceChangePercent;
    private PricingStrategyType strategyType;
    private Double confidenceScore;
    private String reasoning;
    private Boolean autoApplied;
}
