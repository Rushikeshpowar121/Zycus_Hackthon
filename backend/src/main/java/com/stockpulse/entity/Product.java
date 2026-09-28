package com.stockpulse.entity;

import com.stockpulse.enums.DemandLevel;
import com.stockpulse.enums.InventoryStatus;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "current_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "min_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;

    @Column(name = "reorder_threshold")
    private Integer reorderThreshold;

    @Column(name = "max_stock_limit", nullable = false)
    private Integer maxStockLimit;

    @Column(name = "days_in_stock")
    private Integer daysInStock;

    @Column(name = "days_to_expiry")
    private Integer daysToExpiry; // null if not perishable

    @Column(name = "competitor_price_index", precision = 10, scale = 2)
    private BigDecimal competitorPriceIndex;

    @Column(name = "sales_velocity")
    private Double salesVelocity; // units sold per day

    @Column(name = "demand_velocity")
    private Double demandVelocity;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "views_count")
    private Integer viewsCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_strategy_type", nullable = false)
    private PricingStrategyType activeStrategyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InventoryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "demand_level", nullable = false)
    private DemandLevel demandLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        updateComputedAttributes();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateComputedAttributes();
    }

    public void updateComputedAttributes() {
        if (this.reorderThreshold == null && this.reorderPoint != null) {
            this.reorderThreshold = this.reorderPoint;
        } else if (this.reorderPoint == null && this.reorderThreshold != null) {
            this.reorderPoint = this.reorderThreshold;
        }

        if (this.demandVelocity == null && this.salesVelocity != null) {
            this.demandVelocity = this.salesVelocity;
        } else if (this.salesVelocity == null && this.demandVelocity != null) {
            this.salesVelocity = this.demandVelocity;
        }

        int effectiveReorder = this.reorderThreshold != null ? this.reorderThreshold : (this.reorderPoint != null ? this.reorderPoint : 10);

        // Compute Inventory Status
        if (stockQuantity == null || stockQuantity <= 0) {
            this.status = InventoryStatus.OUT_OF_STOCK;
        } else if (stockQuantity <= effectiveReorder) {
            this.status = InventoryStatus.LOW_STOCK;
        } else if (daysToExpiry != null && daysToExpiry <= 14) {
            this.status = InventoryStatus.EXPIRING_SOON;
        } else if (maxStockLimit != null && stockQuantity >= maxStockLimit) {
            this.status = InventoryStatus.OVERSTOCKED;
        } else {
            this.status = InventoryStatus.OPTIMAL;
        }

        // Compute Demand Level
        int views = viewsCount != null ? viewsCount : 0;
        double velocity = salesVelocity != null ? salesVelocity : 0.0;
        if (views > 1000 || velocity > 30.0) {
            this.demandLevel = DemandLevel.SURGE;
        } else if (views > 500 || velocity > 15.0) {
            this.demandLevel = DemandLevel.HIGH;
        } else if (views > 150 || velocity > 5.0) {
            this.demandLevel = DemandLevel.NORMAL;
        } else if (views > 50 || velocity > 1.0) {
            this.demandLevel = DemandLevel.LOW;
        } else {
            this.demandLevel = DemandLevel.VERY_LOW;
        }

        // Compute Risk Level
        if (this.status == InventoryStatus.OUT_OF_STOCK || (daysToExpiry != null && daysToExpiry <= 5)) {
            this.riskLevel = RiskLevel.CRITICAL;
        } else if (this.status == InventoryStatus.LOW_STOCK || (daysToExpiry != null && daysToExpiry <= 14)) {
            this.riskLevel = RiskLevel.HIGH;
        } else if (this.status == InventoryStatus.OVERSTOCKED) {
            this.riskLevel = RiskLevel.MEDIUM;
        } else {
            this.riskLevel = RiskLevel.LOW;
        }
    }
}
