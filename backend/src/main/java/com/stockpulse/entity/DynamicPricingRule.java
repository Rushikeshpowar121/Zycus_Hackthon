package com.stockpulse.entity;

import com.stockpulse.enums.PricingStrategyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pricing_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicPricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_type", nullable = false, unique = true)
    private PricingStrategyType strategyType;

    @Column(name = "surge_multiplier")
    private Double surgeMultiplier; // e.g. 1.15 for +15% during high demand

    @Column(name = "discount_rate")
    private Double discountRate; // e.g. 0.10 for 10% discount on aging inventory

    @Column(name = "competitor_margin_target")
    private Double competitorMarginTarget; // e.g. -0.03 to undercut competitor by 3%

    @Column(name = "max_discount_percent")
    private Double maxDiscountPercent; // cap discount at 25%

    @Column(name = "ai_confidence_score")
    private Double aiConfidenceScore; // 0.00 to 1.00

    @Column(name = "is_active", nullable = false)
    private Boolean active;
}
