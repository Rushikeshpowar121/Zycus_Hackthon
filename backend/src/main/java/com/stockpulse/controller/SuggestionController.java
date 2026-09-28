package com.stockpulse.controller;

import com.stockpulse.dto.ProductDTO;
import com.stockpulse.dto.UpdateStockRequest;
import com.stockpulse.entity.InventoryLog;
import com.stockpulse.entity.PriceHistory;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.InventoryStatus;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.enums.SuggestionStatus;
import com.stockpulse.enums.TriggerReason;
import com.stockpulse.exception.ResourceNotFoundException;
import com.stockpulse.repository.InventoryLogRepository;
import com.stockpulse.repository.PriceHistoryRepository;
import com.stockpulse.repository.PricingSuggestionRepository;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.repository.ReorderSuggestionRepository;
import com.stockpulse.service.AdvisorRegistry;
import com.stockpulse.service.CommerceAdvisor;
import com.stockpulse.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SuggestionController {

    private final PricingSuggestionRepository pricingSuggestionRepository;
    private final ReorderSuggestionRepository reorderSuggestionRepository;
    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductService productService;
    private final AdvisorRegistry advisorRegistry;

    // --- Human Approval Workflow for Pricing Suggestions ---
    @PatchMapping({"/pricing-suggestions/{id}", "/api/v1/pricing-suggestions/{id}", "/api/v1/advisor/pricing-suggestions/{id}"})
    public ResponseEntity<PricingSuggestion> handlePricingSuggestion(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "APPROVED") String action) {
        PricingSuggestion suggestion = pricingSuggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PricingSuggestion not found with id: " + id));

        boolean accept = "ACCEPTED".equalsIgnoreCase(action) || "APPROVED".equalsIgnoreCase(action) || "ACCEPT".equalsIgnoreCase(action);

        if (accept) {
            suggestion.setStatus(SuggestionStatus.APPROVED);

            Product product = productRepository.findById(suggestion.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + suggestion.getProductId()));

            BigDecimal oldPrice = product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice();
            BigDecimal newPrice = suggestion.getSuggestedPrice();
            BigDecimal priceChange = newPrice.subtract(oldPrice);
            double pct = oldPrice.compareTo(BigDecimal.ZERO) > 0 
                    ? priceChange.divide(oldPrice, 4, RoundingMode.HALF_UP).doubleValue() * 100.0 
                    : 0.0;

            product.setCurrentPrice(newPrice);
            product.updateComputedAttributes();
            productRepository.save(product);

            // Audit Price History
            priceHistoryRepository.save(PriceHistory.builder()
                    .productId(product.getId())
                    .productSku(product.getSku())
                    .oldPrice(oldPrice)
                    .newPrice(newPrice)
                    .priceChangePercent(pct)
                    .reason(suggestion.getReason() != null ? suggestion.getReason() : "Accepted Pricing Suggestion")
                    .strategyUsed(PricingStrategyType.AI_HEURISTIC)
                    .timestamp(LocalDateTime.now())
                    .build());

            log.info("Human Approval: Accepted PricingSuggestion ID: {} for Product ID: {}. Price updated: ${} -> ${}",
                    id, product.getId(), oldPrice, newPrice);
        } else {
            suggestion.setStatus(SuggestionStatus.REJECTED);
            log.info("Human Approval: Rejected PricingSuggestion ID: {} for Product ID: {}", id, suggestion.getProductId());
        }

        PricingSuggestion updatedSuggestion = pricingSuggestionRepository.save(suggestion);
        reevaluateProductStatus(suggestion.getProductId());
        return ResponseEntity.ok(updatedSuggestion);
    }

    // --- Human Approval Workflow for Reorder Suggestions ---
    @PatchMapping({"/reorder-suggestions/{id}", "/api/v1/reorder-suggestions/{id}", "/api/v1/advisor/reorder-suggestions/{id}"})
    public ResponseEntity<ReorderSuggestion> handleReorderSuggestion(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "APPROVED") String action) {
        ReorderSuggestion suggestion = reorderSuggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReorderSuggestion not found with id: " + id));

        boolean accept = "ACCEPTED".equalsIgnoreCase(action) || "APPROVED".equalsIgnoreCase(action) || "ACCEPT".equalsIgnoreCase(action);

        if (accept) {
            suggestion.setStatus(SuggestionStatus.APPROVED);

            Product product = productRepository.findById(suggestion.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + suggestion.getProductId()));

            int prevStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int addedQty = suggestion.getSuggestedQuantity() != null ? suggestion.getSuggestedQuantity() : 0;
            int newStock = prevStock + addedQty;

            product.setStockQuantity(newStock);
            product.updateComputedAttributes();
            productRepository.save(product);

            // Audit Inventory Log
            inventoryLogRepository.save(InventoryLog.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .previousStock(prevStock)
                    .newStock(newStock)
                    .quantityChange(addedQty)
                    .action("REORDER_ACCEPTED")
                    .notes(suggestion.getReason() != null ? suggestion.getReason() : "Accepted Reorder Suggestion")
                    .timestamp(LocalDateTime.now())
                    .build());

            log.info("Human Approval: Accepted ReorderSuggestion ID: {} for Product ID: {}. Stock updated: {} -> {}",
                    id, product.getId(), prevStock, newStock);
        } else {
            suggestion.setStatus(SuggestionStatus.REJECTED);
            log.info("Human Approval: Rejected ReorderSuggestion ID: {} for Product ID: {}", id, suggestion.getProductId());
        }

        ReorderSuggestion updatedSuggestion = reorderSuggestionRepository.save(suggestion);
        reevaluateProductStatus(suggestion.getProductId());
        return ResponseEntity.ok(updatedSuggestion);
    }

    // --- Order Simulation Endpoint (POST /products/{id}/orders) ---
    @PostMapping({"/products/{id}/orders", "/api/v1/products/{id}/orders"})
    public ResponseEntity<ProductDTO> createOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Integer quantity) {
        int qty = quantity != null && quantity > 0 ? quantity : 1;
        UpdateStockRequest req = UpdateStockRequest.builder()
                .quantityChange(-qty)
                .action("STOCK_SALE")
                .notes(String.format("Order simulated via POST /products/%d/orders (Qty: %d)", id, qty))
                .build();

        return ResponseEntity.ok(productService.updateStock(id, req));
    }

    // --- On-Demand Pricing Suggestion Endpoint ---
    @PostMapping({"/products/{id}/suggest-pricing", "/api/v1/products/{id}/suggest-pricing"})
    public ResponseEntity<PricingSuggestion> suggestPricing(@PathVariable Long id) {
        Product product = productService.getProductEntityById(id);
        CommerceAdvisor activeAdvisor = advisorRegistry.getActiveAdvisor();
        var bundle = activeAdvisor.recommend(product, TriggerReason.MANUAL);

        BigDecimal currentP = product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice();
        BigDecimal defaultSuggested = currentP.multiply(BigDecimal.valueOf(1.05)).setScale(2, RoundingMode.HALF_UP);

        PricingSuggestion suggestion = bundle != null && bundle.getPricingSuggestion() != null
                ? bundle.getPricingSuggestion()
                : PricingSuggestion.builder()
                .productId(id)
                .oldPrice(currentP)
                .suggestedPrice(defaultSuggested)
                .direction(com.stockpulse.enums.Direction.INCREASE)
                .confidenceScore(0.85)
                .reason("On-Demand Manual Pricing Suggestion")
                .status(SuggestionStatus.PENDING)
                .build();

        suggestion.setProductId(id);
        PricingSuggestion saved = pricingSuggestionRepository.save(suggestion);
        product.setStatus(InventoryStatus.PRICE_REVIEW_PENDING);
        productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    // --- On-Demand Reorder Suggestion Endpoint ---
    @PostMapping({"/products/{id}/suggest-reorder", "/api/v1/products/{id}/suggest-reorder"})
    public ResponseEntity<ReorderSuggestion> suggestReorder(@PathVariable Long id) {
        Product product = productService.getProductEntityById(id);
        int threshold = product.getReorderThreshold() != null ? product.getReorderThreshold() : 20;
        int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int calcQty = Math.max(1, (threshold * 3) - stock);

        ReorderSuggestion suggestion = ReorderSuggestion.builder()
                .productId(id)
                .supplierId(product.getSupplierId() != null ? product.getSupplierId() : 101L)
                .suggestedQuantity(calcQty)
                .suggestedLeadTimeDays(7)
                .reason(String.format("On-Demand Manual Reorder Request. Formula: (Threshold * 3 - Stock) = %d units.", calcQty))
                .status(SuggestionStatus.PENDING)
                .build();

        ReorderSuggestion saved = reorderSuggestionRepository.save(suggestion);
        return ResponseEntity.ok(saved);
    }

    private void reevaluateProductStatus(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        boolean hasPendingPricing = !pricingSuggestionRepository.findByProductIdAndStatus(productId, SuggestionStatus.PENDING).isEmpty();
        boolean hasPendingReorder = !reorderSuggestionRepository.findByProductIdAndStatus(productId, SuggestionStatus.PENDING).isEmpty();

        if (!hasPendingPricing && !hasPendingReorder) {
            product.updateComputedAttributes();
            productRepository.save(product);
        }
    }
}
