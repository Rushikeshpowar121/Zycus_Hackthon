package com.stockpulse.controller;

import com.stockpulse.dto.AnalyticsSummaryDTO;
import com.stockpulse.dto.PriceAuditDTO;
import com.stockpulse.entity.InventoryLog;
import com.stockpulse.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDTO> getSummary() {
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary());
    }

    @GetMapping("/price-audit/{productId}")
    public ResponseEntity<List<PriceAuditDTO>> getPriceAuditHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(analyticsService.getPriceAuditHistory(productId));
    }

    @GetMapping("/price-audit/recent")
    public ResponseEntity<List<PriceAuditDTO>> getRecentPriceAudits() {
        return ResponseEntity.ok(analyticsService.getRecentPriceAudits());
    }

    @GetMapping("/inventory-logs")
    public ResponseEntity<List<InventoryLog>> getRecentInventoryLogs() {
        return ResponseEntity.ok(analyticsService.getRecentInventoryLogs());
    }
}
