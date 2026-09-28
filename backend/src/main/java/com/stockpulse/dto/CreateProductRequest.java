package com.stockpulse.dto;

import com.stockpulse.enums.PricingStrategyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    @NotNull(message = "Cost price is required")
    @Positive(message = "Cost price must be positive")
    private BigDecimal costPrice;

    @NotNull(message = "Min price is required")
    @Positive(message = "Min price must be positive")
    private BigDecimal minPrice;

    @NotNull(message = "Max price is required")
    @Positive(message = "Max price must be positive")
    private BigDecimal maxPrice;

    @NotNull(message = "Initial stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Reorder point is required")
    @Min(value = 1, message = "Reorder point must be at least 1")
    private Integer reorderPoint;

    @NotNull(message = "Max stock limit is required")
    @Min(value = 1, message = "Max stock limit must be at least 1")
    private Integer maxStockLimit;

    private Integer daysInStock;
    private Integer daysToExpiry;
    private BigDecimal competitorPriceIndex;
    private Double salesVelocity;
    private Integer viewsCount;

    @NotNull(message = "Active strategy type is required")
    private PricingStrategyType activeStrategyType;
}
