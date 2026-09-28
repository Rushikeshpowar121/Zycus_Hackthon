package com.stockpulse.dto;

import com.stockpulse.enums.PricingStrategyType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStrategyRequest {

    @NotNull(message = "Strategy type is required")
    private PricingStrategyType strategyType;
    
    private Double surgeMultiplier;
    private Double discountRate;
    private Double competitorMarginTarget;
    private Double maxDiscountPercent;
}
