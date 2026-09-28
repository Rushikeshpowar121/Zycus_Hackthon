package com.stockpulse.controller;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.dto.UpdateStrategyRequest;
import com.stockpulse.entity.DynamicPricingRule;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.service.PricingEngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
public class DynamicPricingController {

    private final PricingEngineService pricingEngineService;

    @GetMapping("/recommendation/{productId}")
    public ResponseEntity<PricingCalculationResultDTO> getRecommendation(
            @PathVariable Long productId,
            @RequestParam(required = false) PricingStrategyType overrideStrategy) {
        return ResponseEntity.ok(pricingEngineService.calculateRecommendation(productId, overrideStrategy));
    }

    @PostMapping("/apply/{productId}")
    public ResponseEntity<ProductDTO> applyRecommendation(
            @PathVariable Long productId,
            @RequestBody PricingCalculationResultDTO recommendation) {
        return ResponseEntity.ok(pricingEngineService.applyPriceRecommendation(productId, recommendation));
    }

    @PostMapping("/batch-reprice")
    public ResponseEntity<List<PricingCalculationResultDTO>> batchReprice() {
        return ResponseEntity.ok(pricingEngineService.batchRepriceAll());
    }

    @GetMapping("/rules")
    public ResponseEntity<List<DynamicPricingRule>> getAllRules() {
        return ResponseEntity.ok(pricingEngineService.getAllPricingRules());
    }

    @GetMapping("/rules/{strategyType}")
    public ResponseEntity<DynamicPricingRule> getRule(@PathVariable PricingStrategyType strategyType) {
        return ResponseEntity.ok(pricingEngineService.getPricingRule(strategyType));
    }

    @PutMapping("/rules")
    public ResponseEntity<DynamicPricingRule> updateRule(@Valid @RequestBody UpdateStrategyRequest request) {
        return ResponseEntity.ok(pricingEngineService.updatePricingRule(request));
    }
}
