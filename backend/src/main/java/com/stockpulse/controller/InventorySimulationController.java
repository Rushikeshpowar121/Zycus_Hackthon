package com.stockpulse.controller;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.dto.StockSimulationRequest;
import com.stockpulse.service.InventorySimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulation")
@RequiredArgsConstructor
public class InventorySimulationController {

    private final InventorySimulationService simulationService;

    @PostMapping("/run")
    public ResponseEntity<PricingCalculationResultDTO> runSimulation(@RequestBody StockSimulationRequest request) {
        return ResponseEntity.ok(simulationService.runSimulation(request));
    }
}
