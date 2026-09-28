package com.stockpulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSimulationRequest {
    private Long productId;
    private String simulationType; // DEMAND_SURGE, COMPETITOR_PRICE_DROP, AGING_SPIKE, FLASH_SALE
    private Integer viewMultiplier; // e.g. 5x views
    private Double salesVelocityBoost; // e.g. +20 units/day
    private BigDecimal competitorPriceAdjustment; // new competitor price
    private Integer reduceExpiryDaysBy; // simulate aging
}
