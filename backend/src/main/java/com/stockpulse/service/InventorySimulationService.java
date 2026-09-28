package com.stockpulse.service;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.dto.StockSimulationRequest;

public interface InventorySimulationService {
    PricingCalculationResultDTO runSimulation(StockSimulationRequest request);
}
