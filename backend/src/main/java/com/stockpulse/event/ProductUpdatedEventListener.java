package com.stockpulse.event;

import com.stockpulse.dto.RecommendationBundle;
import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.entity.Product;
import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.SuggestionStatus;
import com.stockpulse.enums.TriggerReason;
import com.stockpulse.repository.PricingSuggestionRepository;
import com.stockpulse.repository.ProductRepository;
import com.stockpulse.repository.ReorderSuggestionRepository;
import com.stockpulse.service.AdvisorRegistry;
import com.stockpulse.service.CommerceAdvisor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductUpdatedEventListener {

    private final ProductRepository productRepository;
    private final PricingSuggestionRepository pricingSuggestionRepository;
    private final ReorderSuggestionRepository reorderSuggestionRepository;
    private final AdvisorRegistry advisorRegistry;

    @Async
    @EventListener
    @Transactional
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("[Async Agentic Loop - Thread: {}] Processing ProductUpdatedEvent for Product ID: {} (SKU: {}, EventType: {})",
                Thread.currentThread().getName(), event.getProductId(), event.getSku(), event.getEventType());

        Product product = productRepository.findById(event.getProductId()).orElse(null);
        if (product == null) {
            log.warn("Product ID {} not found during agentic event processing.", event.getProductId());
            return;
        }

        int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int threshold = product.getReorderThreshold() != null ? product.getReorderThreshold() :
                (product.getReorderPoint() != null ? product.getReorderPoint() : 10);

        double demandVel = product.getDemandVelocity() != null ? product.getDemandVelocity() :
                (product.getSalesVelocity() != null ? product.getSalesVelocity() : 0.0);

        double categoryAvgVel = getCategoryAverageVelocity(product.getCategory());

        TriggerReason triggerReason = null;

        if (stock < threshold) {
            triggerReason = TriggerReason.INVENTORY_LOW;
        } else if (categoryAvgVel > 0 && demandVel > (categoryAvgVel * 3.0)) {
            triggerReason = TriggerReason.DEMAND_SPIKE;
        }

        if (triggerReason == null) {
            log.info("Agentic Loop: Product {} state evaluated. No INVENTORY_LOW or DEMAND_SPIKE triggers met.", product.getSku());
            return;
        }

        log.info("Agentic Loop: Triggered {} for SKU: {} (Stock: {}, Threshold: {}, DemandVel: {}, CatAvg: {})",
                triggerReason, product.getSku(), stock, threshold, demandVel, categoryAvgVel);

        // Fetch active CommerceAdvisor dynamically from AdvisorRegistry
        CommerceAdvisor activeAdvisor = advisorRegistry.getActiveAdvisor();
        RecommendationBundle bundle = activeAdvisor.recommend(product, triggerReason);

        if (bundle == null) {
            return;
        }

        // Avoid duplicate pending pricing suggestions
        List<PricingSuggestion> existingPendingPricing = pricingSuggestionRepository.findByProductIdAndStatus(
                product.getId(), SuggestionStatus.PENDING);

        boolean suggestionCreated = false;

        if (existingPendingPricing.isEmpty() && bundle.getPricingSuggestion() != null) {
            PricingSuggestion p = bundle.getPricingSuggestion();
            p.setProductId(product.getId());
            PricingSuggestion savedPricing = pricingSuggestionRepository.save(p);
            suggestionCreated = true;
            log.info("Agentic Loop: Created new PricingSuggestion ID: {} for SKU: {} (Price: ${} -> ${}, Direction: {})",
                    savedPricing.getId(), product.getSku(), p.getOldPrice(), p.getSuggestedPrice(), p.getDirection());
        } else {
            log.info("Agentic Loop: Skipped duplicate pending PricingSuggestion for product ID: {}", product.getId());
        }

        // Avoid duplicate pending reorder suggestions
        List<ReorderSuggestion> existingPendingReorder = reorderSuggestionRepository.findByProductIdAndStatus(
                product.getId(), SuggestionStatus.PENDING);

        if (existingPendingReorder.isEmpty() && bundle.getReorderSuggestion() != null) {
            ReorderSuggestion r = bundle.getReorderSuggestion();
            r.setProductId(product.getId());
            ReorderSuggestion savedReorder = reorderSuggestionRepository.save(r);
            suggestionCreated = true;
            log.info("Agentic Loop: Created new ReorderSuggestion ID: {} for SKU: {} (Suggested Qty: {} units)",
                    savedReorder.getId(), product.getSku(), r.getSuggestedQuantity());
        } else if (bundle.getReorderSuggestion() == null && stock < threshold) {
            // Fallback reorder suggestion generation if stock < threshold
            if (existingPendingReorder.isEmpty()) {
                int calcQty = Math.max(1, (threshold * 3) - stock);
                ReorderSuggestion fallbackReorder = ReorderSuggestion.builder()
                        .productId(product.getId())
                        .supplierId(product.getSupplierId() != null ? product.getSupplierId() : 101L)
                        .suggestedQuantity(calcQty)
                        .suggestedLeadTimeDays(7)
                        .reason(String.format("Agentic Loop Trigger (%s): Stock (%d) < Threshold (%d). Formula: (Threshold * 3 - Stock) = %d units.",
                                triggerReason, stock, threshold, calcQty))
                        .status(SuggestionStatus.PENDING)
                        .build();
                ReorderSuggestion savedReorder = reorderSuggestionRepository.save(fallbackReorder);
                suggestionCreated = true;
                log.info("Agentic Loop: Created fallback ReorderSuggestion ID: {} for SKU: {} (Qty: {} units)",
                        savedReorder.getId(), product.getSku(), calcQty);
            }
        } else {
            log.info("Agentic Loop: Skipped duplicate pending ReorderSuggestion for product ID: {}", product.getId());
        }

        if (suggestionCreated) {
            product.setStatus(com.stockpulse.enums.InventoryStatus.PRICE_REVIEW_PENDING);
            productRepository.save(product);
            log.info("Agentic Loop: Updated Product {} status to PRICE_REVIEW_PENDING", product.getSku());
        }
    }

    private double getCategoryAverageVelocity(String category) {
        if (category == null || category.isBlank()) return 0.0;
        List<Product> categoryProducts = productRepository.findByCategory(category);
        if (categoryProducts == null || categoryProducts.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Product p : categoryProducts) {
            double v = p.getDemandVelocity() != null ? p.getDemandVelocity() :
                    (p.getSalesVelocity() != null ? p.getSalesVelocity() : 0.0);
            sum += v;
        }
        return sum / categoryProducts.size();
    }
}
