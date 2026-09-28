package com.stockpulse.service;

import com.stockpulse.dto.RecommendationBundle;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.TriggerReason;

public interface CommerceAdvisor {
    RecommendationBundle recommend(
        Product product,
        TriggerReason triggerReason
    );
}
