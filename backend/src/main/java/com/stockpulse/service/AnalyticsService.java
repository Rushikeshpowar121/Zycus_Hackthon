package com.stockpulse.service;

import com.stockpulse.dto.AnalyticsSummaryDTO;
import com.stockpulse.dto.PriceAuditDTO;
import com.stockpulse.entity.InventoryLog;

import java.util.List;

public interface AnalyticsService {
    AnalyticsSummaryDTO getAnalyticsSummary();
    List<PriceAuditDTO> getPriceAuditHistory(Long productId);
    List<PriceAuditDTO> getRecentPriceAudits();
    List<InventoryLog> getRecentInventoryLogs();
}
