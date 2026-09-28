package com.stockpulse.service;

import com.stockpulse.dto.CreateProductRequest;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.repository.DynamicPricingRuleRepository;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PricingEngineService pricingEngineService;
    private final DynamicPricingRuleRepository ruleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0) {
            return;
        }

        log.info("Seeding StockPulse initial AI Inventory & Dynamic Pricing Data...");

        // Initialize Dynamic Pricing Rules
        pricingEngineService.getPricingRule(PricingStrategyType.DEMAND_BASED);
        pricingEngineService.getPricingRule(PricingStrategyType.COMPETITOR_BASED);
        pricingEngineService.getPricingRule(PricingStrategyType.AGING_INVENTORY);
        pricingEngineService.getPricingRule(PricingStrategyType.AI_HEURISTIC);

        // Product 1: High-end Wireless Headphones (Demand Surge)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SP-ELEC-901")
                .name("PulseSonic Pro ANC Headphones")
                .category("Electronics")
                .basePrice(new BigDecimal("299.99"))
                .costPrice(new BigDecimal("140.00"))
                .minPrice(new BigDecimal("220.00"))
                .maxPrice(new BigDecimal("399.99"))
                .stockQuantity(18)
                .reorderPoint(25)
                .maxStockLimit(100)
                .daysInStock(12)
                .salesVelocity(28.5)
                .viewsCount(1420)
                .competitorPriceIndex(new BigDecimal("315.00"))
                .activeStrategyType(PricingStrategyType.DEMAND_BASED)
                .build());

        // Product 2: Perishable Organic Avocado Oil (Expiring Soon)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SP-GROC-402")
                .name("Artisan Organic Extra Virgin Avocado Oil 500ml")
                .category("Groceries")
                .basePrice(new BigDecimal("19.99"))
                .costPrice(new BigDecimal("8.50"))
                .minPrice(new BigDecimal("10.99"))
                .maxPrice(new BigDecimal("24.99"))
                .stockQuantity(150)
                .reorderPoint(40)
                .maxStockLimit(200)
                .daysInStock(45)
                .daysToExpiry(6) // Expiring soon!
                .salesVelocity(4.2)
                .viewsCount(180)
                .competitorPriceIndex(new BigDecimal("18.50"))
                .activeStrategyType(PricingStrategyType.AGING_INVENTORY)
                .build());

        // Product 3: Smart 4K Gaming Monitor (Competitor Price Pressure)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SP-ELEC-774")
                .name("ApexVision 32\" 144Hz 4K Gaming Monitor")
                .category("Electronics")
                .basePrice(new BigDecimal("549.99"))
                .costPrice(new BigDecimal("320.00"))
                .minPrice(new BigDecimal("420.00"))
                .maxPrice(new BigDecimal("699.99"))
                .stockQuantity(42)
                .reorderPoint(15)
                .maxStockLimit(80)
                .daysInStock(25)
                .salesVelocity(12.0)
                .viewsCount(890)
                .competitorPriceIndex(new BigDecimal("499.00")) // Competitor undercutting
                .activeStrategyType(PricingStrategyType.COMPETITOR_BASED)
                .build());

        // Product 4: Ergonomic Mesh Office Chair (AI Heuristic Optimization)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SP-FURN-108")
                .name("ErgoPulse Lumbar Mesh Executive Chair")
                .category("Furniture")
                .basePrice(new BigDecimal("349.00"))
                .costPrice(new BigDecimal("165.00"))
                .minPrice(new BigDecimal("249.00"))
                .maxPrice(new BigDecimal("449.00"))
                .stockQuantity(85)
                .reorderPoint(20)
                .maxStockLimit(120)
                .daysInStock(15)
                .salesVelocity(18.0)
                .viewsCount(620)
                .competitorPriceIndex(new BigDecimal("379.00"))
                .activeStrategyType(PricingStrategyType.AI_HEURISTIC)
                .build());

        // Product 5: Thermal Compression Winter Jacket (Overstocked Aging)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SP-APPA-311")
                .name("NordicShield Waterproof Heated Parka")
                .category("Apparel")
                .basePrice(new BigDecimal("189.99"))
                .costPrice(new BigDecimal("75.00"))
                .minPrice(new BigDecimal("109.99"))
                .maxPrice(new BigDecimal("229.99"))
                .stockQuantity(165) // Overstocked
                .reorderPoint(30)
                .maxStockLimit(100)
                .daysInStock(95) // Aging over 90 days
                .salesVelocity(1.5)
                .viewsCount(95)
                .competitorPriceIndex(new BigDecimal("175.00"))
                .activeStrategyType(PricingStrategyType.AGING_INVENTORY)
                .build());

        // Product 6: High Performance Mechanical Keyboard
        productService.createProduct(CreateProductRequest.builder()
                .sku("SP-ELEC-112")
                .name("Vortex RGB Wireless Mechanical Keyboard")
                .category("Electronics")
                .basePrice(new BigDecimal("129.99"))
                .costPrice(new BigDecimal("55.00"))
                .minPrice(new BigDecimal("89.99"))
                .maxPrice(new BigDecimal("169.99"))
                .stockQuantity(0) // Out of stock!
                .reorderPoint(20)
                .maxStockLimit(100)
                .daysInStock(5)
                .salesVelocity(35.0)
                .viewsCount(1950)
                .competitorPriceIndex(new BigDecimal("139.99"))
                .activeStrategyType(PricingStrategyType.DEMAND_BASED)
                .build());

        log.info("Batch evaluating initial AI pricing recommendations...");
        pricingEngineService.batchRepriceAll();

        log.info("StockPulse initial data seeding complete!");
    }
}
