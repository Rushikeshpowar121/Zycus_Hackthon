package com.stockpulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummaryDTO {
    private long totalProducts;
    private long optimalStockCount;
    private long lowStockCount;
    private long outOfStockCount;
    private long overstockedCount;
    private long expiringSoonCount;
    private long criticalRiskCount;
    private BigDecimal totalInventoryValue;
    private BigDecimal totalPotentialRevenue;
    private Double averageProfitMarginPercent;
    private long totalRepricedEvents;
    private Map<String, Long> strategyDistribution;
    private Map<String, Long> categoryDistribution;
}
