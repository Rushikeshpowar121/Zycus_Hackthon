package com.stockpulse.service;

import com.stockpulse.dto.AnalyticsSummaryDTO;
import com.stockpulse.dto.PriceAuditDTO;
import com.stockpulse.entity.InventoryLog;
import com.stockpulse.entity.PriceHistory;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.InventoryStatus;
import com.stockpulse.enums.RiskLevel;
import com.stockpulse.repository.InventoryLogRepository;
import com.stockpulse.repository.PriceHistoryRepository;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryDTO getAnalyticsSummary() {
        List<Product> products = productRepository.findAll();
        long totalProducts = products.size();

        long optimalCount = productRepository.countByStatus(InventoryStatus.OPTIMAL);
        long lowStockCount = productRepository.countByStatus(InventoryStatus.LOW_STOCK);
        long outOfStockCount = productRepository.countByStatus(InventoryStatus.OUT_OF_STOCK);
        long overstockedCount = productRepository.countByStatus(InventoryStatus.OVERSTOCKED);
        long expiringSoonCount = productRepository.countByStatus(InventoryStatus.EXPIRING_SOON);
        long criticalRiskCount = productRepository.countByRiskLevel(RiskLevel.CRITICAL);

        BigDecimal totalInventoryValue = BigDecimal.ZERO;
        BigDecimal totalPotentialRevenue = BigDecimal.ZERO;
        double totalMarginPercentSum = 0.0;

        Map<String, Long> strategyDistribution = new HashMap<>();
        Map<String, Long> categoryDistribution = new HashMap<>();

        for (Product p : products) {
            BigDecimal qty = BigDecimal.valueOf(p.getStockQuantity());
            totalInventoryValue = totalInventoryValue.add(p.getCostPrice().multiply(qty));
            totalPotentialRevenue = totalPotentialRevenue.add(p.getCurrentPrice().multiply(qty));

            if (p.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
                double margin = p.getCurrentPrice().subtract(p.getCostPrice())
                        .divide(p.getCurrentPrice(), 4, RoundingMode.HALF_UP)
                        .doubleValue() * 100.0;
                totalMarginPercentSum += margin;
            }

            String stratKey = p.getActiveStrategyType() != null ? p.getActiveStrategyType().name() : "MANUAL_OVERRIDE";
            strategyDistribution.put(stratKey, strategyDistribution.getOrDefault(stratKey, 0L) + 1);

            String catKey = p.getCategory() != null ? p.getCategory() : "Uncategorized";
            categoryDistribution.put(catKey, categoryDistribution.getOrDefault(catKey, 0L) + 1);
        }

        double avgMargin = totalProducts > 0 ? (totalMarginPercentSum / totalProducts) : 0.0;
        long totalRepricedEvents = priceHistoryRepository.count();

        return AnalyticsSummaryDTO.builder()
                .totalProducts(totalProducts)
                .optimalStockCount(optimalCount)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .overstockedCount(overstockedCount)
                .expiringSoonCount(expiringSoonCount)
                .criticalRiskCount(criticalRiskCount)
                .totalInventoryValue(totalInventoryValue.setScale(2, RoundingMode.HALF_UP))
                .totalPotentialRevenue(totalPotentialRevenue.setScale(2, RoundingMode.HALF_UP))
                .averageProfitMarginPercent(Math.round(avgMargin * 100.0) / 100.0)
                .totalRepricedEvents(totalRepricedEvents)
                .strategyDistribution(strategyDistribution)
                .categoryDistribution(categoryDistribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceAuditDTO> getPriceAuditHistory(Long productId) {
        return priceHistoryRepository.findByProductIdOrderByTimestampDesc(productId)
                .stream()
                .map(this::mapToAuditDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceAuditDTO> getRecentPriceAudits() {
        return priceHistoryRepository.findTop50ByOrderByTimestampDesc()
                .stream()
                .map(this::mapToAuditDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryLog> getRecentInventoryLogs() {
        return inventoryLogRepository.findTop50ByOrderByTimestampDesc();
    }

    private PriceAuditDTO mapToAuditDTO(PriceHistory ph) {
        return PriceAuditDTO.builder()
                .id(ph.getId())
                .productId(ph.getProductId())
                .productSku(ph.getProductSku())
                .oldPrice(ph.getOldPrice())
                .newPrice(ph.getNewPrice())
                .priceChangePercent(ph.getPriceChangePercent())
                .strategyUsed(ph.getStrategyUsed())
                .reason(ph.getReason())
                .timestamp(ph.getTimestamp())
                .build();
    }
}
