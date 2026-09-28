package com.stockpulse.strategy;

import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.exception.InvalidStrategyException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyRegistry {

    private final Map<PricingStrategyType, PricingStrategy> strategyMap;

    public StrategyRegistry(List<PricingStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(PricingStrategy::getType, Function.identity()));
    }

    public PricingStrategy getStrategy(PricingStrategyType strategyType) {
        return Optional.ofNullable(strategyMap.get(strategyType))
                .orElseThrow(() -> new InvalidStrategyException("Unsupported pricing strategy: " + strategyType));
    }
}
