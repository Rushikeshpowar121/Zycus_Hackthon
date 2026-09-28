package com.stockpulse.service;

import com.stockpulse.dto.PricingCalculationResultDTO;
import com.stockpulse.dto.StockSimulationRequest;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventorySimulationServiceImpl implements InventorySimulationService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final PricingEngineService pricingEngineService;

    @Override
    @Transactional
    public PricingCalculationResultDTO runSimulation(StockSimulationRequest request) {
        Product product = productService.getProductEntityById(request.getProductId());
        String type = request.getSimulationType() != null ? request.getSimulationType().toUpperCase() : "DEMAND_SURGE";

        switch (type) {
            case "DEMAND_SURGE":
                int mult = request.getViewMultiplier() != null ? request.getViewMultiplier() : 4;
                double boost = request.getSalesVelocityBoost() != null ? request.getSalesVelocityBoost() : 25.0;
                product.setViewsCount((product.getViewsCount() != null ? product.getViewsCount() : 100) * mult);
                product.setSalesVelocity((product.getSalesVelocity() != null ? product.getSalesVelocity() : 5.0) + boost);
                product.setActiveStrategyType(PricingStrategyType.DEMAND_BASED);
                break;

            case "COMPETITOR_PRICE_DROP":
                if (request.getCompetitorPriceAdjustment() != null) {
                    product.setCompetitorPriceIndex(request.getCompetitorPriceAdjustment());
                } else if (product.getCompetitorPriceIndex() != null) {
                    product.setCompetitorPriceIndex(product.getCompetitorPriceIndex().multiply(java.math.BigDecimal.valueOf(0.85)));
                } else {
                    product.setCompetitorPriceIndex(product.getBasePrice().multiply(java.math.BigDecimal.valueOf(0.85)));
                }
                product.setActiveStrategyType(PricingStrategyType.COMPETITOR_BASED);
                break;

            case "AGING_SPIKE":
                int daysToReduce = request.getReduceExpiryDaysBy() != null ? request.getReduceExpiryDaysBy() : 10;
                int currentDaysInStock = product.getDaysInStock() != null ? product.getDaysInStock() : 20;
                product.setDaysInStock(currentDaysInStock + 45);
                if (product.getDaysToExpiry() != null) {
                    product.setDaysToExpiry(Math.max(1, product.getDaysToExpiry() - daysToReduce));
                }
                product.setActiveStrategyType(PricingStrategyType.AGING_INVENTORY);
                break;

            case "FLASH_SALE":
                product.setViewsCount(2500);
                product.setSalesVelocity(50.0);
                product.setActiveStrategyType(PricingStrategyType.AI_HEURISTIC);
                break;

            default:
                product.setActiveStrategyType(PricingStrategyType.AI_HEURISTIC);
                break;
        }

        product.updateComputedAttributes();
        productRepository.save(product);

        // Run pricing algorithm based on new simulated state
        return pricingEngineService.calculateRecommendation(product.getId(), null);
    }
}
