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

        log.info("Seeding StockPulse — Hackathon Demo Data (Addendum A spec products)...");

        // Initialize Dynamic Pricing Rules
        pricingEngineService.getPricingRule(PricingStrategyType.DEMAND_BASED);
        pricingEngineService.getPricingRule(PricingStrategyType.COMPETITOR_BASED);
        pricingEngineService.getPricingRule(PricingStrategyType.AGING_INVENTORY);
        pricingEngineService.getPricingRule(PricingStrategyType.AI_HEURISTIC);

        // PRD-001: Wireless Earbuds Pro (ELECTRONICS, stock=45, threshold=20, velocity=3)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-ELEC-001")
                .name("Wireless Earbuds Pro")
                .category("ELECTRONICS")
                .basePrice(new BigDecimal("79.99"))
                .costPrice(new BigDecimal("38.00"))
                .minPrice(new BigDecimal("59.99"))
                .maxPrice(new BigDecimal("109.99"))
                .stockQuantity(45)
                .reorderPoint(20)
                .maxStockLimit(150)
                .daysInStock(8)
                .salesVelocity(3.0)
                .viewsCount(310)
                .competitorPriceIndex(new BigDecimal("74.99"))
                .activeStrategyType(PricingStrategyType.DEMAND_BASED)
                .build());

        // PRD-002: USB-C Hub 7-Port (ELECTRONICS, stock=120, threshold=30, velocity=1)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-ELEC-002")
                .name("USB-C Hub 7-Port")
                .category("ELECTRONICS")
                .basePrice(new BigDecimal("34.99"))
                .costPrice(new BigDecimal("15.00"))
                .minPrice(new BigDecimal("24.99"))
                .maxPrice(new BigDecimal("49.99"))
                .stockQuantity(120)
                .reorderPoint(30)
                .maxStockLimit(200)
                .daysInStock(22)
                .salesVelocity(1.0)
                .viewsCount(85)
                .competitorPriceIndex(new BigDecimal("32.99"))
                .activeStrategyType(PricingStrategyType.COMPETITOR_BASED)
                .build());

        // PRD-003: Organic Cotton T-Shirt (APPAREL, stock=8, threshold=15, velocity=12)
        // DEMO PATH: Stock already below threshold → immediate INVENTORY_LOW trigger possible
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-APP-001")
                .name("Organic Cotton T-Shirt")
                .category("APPAREL")
                .basePrice(new BigDecimal("24.99"))
                .costPrice(new BigDecimal("10.00"))
                .minPrice(new BigDecimal("18.99"))
                .maxPrice(new BigDecimal("34.99"))
                .stockQuantity(8)
                .reorderPoint(15)
                .maxStockLimit(100)
                .daysInStock(14)
                .salesVelocity(12.0)
                .viewsCount(620)
                .competitorPriceIndex(new BigDecimal("22.99"))
                .activeStrategyType(PricingStrategyType.DEMAND_BASED)
                .build());

        // PRD-004: Running Shorts — Navy (APPAREL, stock=55, threshold=20, velocity=2)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-APP-002")
                .name("Running Shorts — Navy")
                .category("APPAREL")
                .basePrice(new BigDecimal("39.99"))
                .costPrice(new BigDecimal("16.00"))
                .minPrice(new BigDecimal("29.99"))
                .maxPrice(new BigDecimal("54.99"))
                .stockQuantity(55)
                .reorderPoint(20)
                .maxStockLimit(150)
                .daysInStock(30)
                .salesVelocity(2.0)
                .viewsCount(120)
                .competitorPriceIndex(new BigDecimal("37.99"))
                .activeStrategyType(PricingStrategyType.DEMAND_BASED)
                .build());

        // PRD-005: Ceramic Pour-Over Set (HOME, stock=22, threshold=10, velocity=4)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-HOME-001")
                .name("Ceramic Pour-Over Set")
                .category("HOME")
                .basePrice(new BigDecimal("49.99"))
                .costPrice(new BigDecimal("22.00"))
                .minPrice(new BigDecimal("37.99"))
                .maxPrice(new BigDecimal("67.99"))
                .stockQuantity(22)
                .reorderPoint(10)
                .maxStockLimit(80)
                .daysInStock(18)
                .salesVelocity(4.0)
                .viewsCount(210)
                .competitorPriceIndex(new BigDecimal("47.99"))
                .activeStrategyType(PricingStrategyType.AI_HEURISTIC)
                .build());

        // PRD-006: LED Desk Lamp — Dimmable (HOME, stock=0, threshold=15, velocity=0) — OUT OF STOCK
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-HOME-002")
                .name("LED Desk Lamp — Dimmable")
                .category("HOME")
                .basePrice(new BigDecimal("59.99"))
                .costPrice(new BigDecimal("26.00"))
                .minPrice(new BigDecimal("44.99"))
                .maxPrice(new BigDecimal("79.99"))
                .stockQuantity(0)
                .reorderPoint(15)
                .maxStockLimit(80)
                .daysInStock(0)
                .salesVelocity(0.0)
                .viewsCount(45)
                .competitorPriceIndex(new BigDecimal("55.99"))
                .activeStrategyType(PricingStrategyType.DEMAND_BASED)
                .build());

        // PRD-007: Portable Charger 20K (ELECTRONICS, stock=18, threshold=25, velocity=8)
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-ELEC-003")
                .name("Portable Charger 20K")
                .category("ELECTRONICS")
                .basePrice(new BigDecimal("44.99"))
                .costPrice(new BigDecimal("19.00"))
                .minPrice(new BigDecimal("34.99"))
                .maxPrice(new BigDecimal("59.99"))
                .stockQuantity(18)
                .reorderPoint(25)
                .maxStockLimit(120)
                .daysInStock(10)
                .salesVelocity(8.0)
                .viewsCount(480)
                .competitorPriceIndex(new BigDecimal("42.99"))
                .activeStrategyType(PricingStrategyType.DEMAND_BASED)
                .build());

        // PRD-008: Hoodie — Heather Grey (APPAREL, stock=11, threshold=12, velocity=15)
        // DEMO PATH: POST multiple orders → velocity > 3x category avg (≈4.3) → DEMAND_SPIKE
        // velocity=15 already >3x avg, one more sale triggers both suggestions
        productService.createProduct(CreateProductRequest.builder()
                .sku("SKU-APP-003")
                .name("Hoodie — Heather Grey")
                .category("APPAREL")
                .basePrice(new BigDecimal("54.99"))
                .costPrice(new BigDecimal("22.00"))
                .minPrice(new BigDecimal("41.99"))
                .maxPrice(new BigDecimal("74.99"))
                .stockQuantity(11)
                .reorderPoint(12)
                .maxStockLimit(80)
                .daysInStock(6)
                .salesVelocity(15.0)
                .viewsCount(890)
                .competitorPriceIndex(new BigDecimal("52.99"))
                .activeStrategyType(PricingStrategyType.AI_HEURISTIC)
                .build());

        log.info("Batch evaluating initial pricing recommendations...");
        pricingEngineService.batchRepriceAll();

        log.info("StockPulse hackathon demo seed complete — 8 products ready (PRD-001..PRD-008 spec).");
    }
}
