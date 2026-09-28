package com.stockpulse.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AsyncEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishStockChange(StockLevelChangeEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public void publishPriceUpdated(DynamicPriceUpdatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public void publishProductUpdated(ProductUpdatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
