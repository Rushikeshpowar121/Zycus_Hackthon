package com.stockpulse.dto;

import com.stockpulse.enums.DemandLevel;
import com.stockpulse.enums.InventoryStatus;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.enums.RiskLevel;
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
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal currentPrice;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer stockQuantity;
    private Integer reorderPoint;
    private Integer maxStockLimit;
    private Integer daysInStock;
    private Integer daysToExpiry;
    private BigDecimal competitorPriceIndex;
    private Double salesVelocity;
    private Integer viewsCount;
    private PricingStrategyType activeStrategyType;
    private InventoryStatus status;
    private DemandLevel demandLevel;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
