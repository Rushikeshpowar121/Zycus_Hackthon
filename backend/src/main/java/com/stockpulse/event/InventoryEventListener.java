package com.stockpulse.event;

import com.stockpulse.entity.InventoryLog;
import com.stockpulse.entity.PriceHistory;
import com.stockpulse.repository.InventoryLogRepository;
import com.stockpulse.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryLogRepository inventoryLogRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Async
    @EventListener
    public void handleStockLevelChange(StockLevelChangeEvent event) {
        log.info("[Async Thread: {}] Processing Stock Level Change Event for Product ID: {} (SKU: {}) - Action: {}",
                Thread.currentThread().getName(), event.getProductId(), event.getSku(), event.getAction());

        InventoryLog logEntity = InventoryLog.builder()
                .productId(event.getProductId())
                .sku(event.getSku())
                .action(event.getAction())
                .quantityChange(event.getNewStock() - event.getPreviousStock())
                .previousStock(event.getPreviousStock())
                .newStock(event.getNewStock())
                .notes(event.getNotes())
                .build();

        inventoryLogRepository.save(logEntity);
    }

    @Async
    @EventListener
    public void handleDynamicPriceUpdated(DynamicPriceUpdatedEvent event) {
        log.info("[Async Thread: {}] Processing Price Update Event for Product ID: {} (SKU: {}) - Old Price: ${}, New Price: ${}",
                Thread.currentThread().getName(), event.getProductId(), event.getSku(), event.getOldPrice(), event.getNewPrice());

        double changePercent = 0.0;
        if (event.getOldPrice() != null && event.getOldPrice().doubleValue() > 0) {
            changePercent = event.getNewPrice().subtract(event.getOldPrice())
                    .divide(event.getOldPrice(), 4, RoundingMode.HALF_UP)
                    .doubleValue() * 100.0;
        }

        PriceHistory history = PriceHistory.builder()
                .productId(event.getProductId())
                .productSku(event.getSku())
                .oldPrice(event.getOldPrice())
                .newPrice(event.getNewPrice())
                .priceChangePercent(Math.round(changePercent * 100.0) / 100.0)
                .strategyUsed(event.getStrategyUsed())
                .reason(event.getReason())
                .build();

        priceHistoryRepository.save(history);
    }
}
