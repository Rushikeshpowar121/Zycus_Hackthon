package com.stockpulse.repository;

import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.enums.PricingStrategyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DynamicPricingRuleRepository extends JpaRepository<DynamicPricingRule, Long> {
    Optional<DynamicPricingRule> findByStrategyType(PricingStrategyType strategyType);
}
