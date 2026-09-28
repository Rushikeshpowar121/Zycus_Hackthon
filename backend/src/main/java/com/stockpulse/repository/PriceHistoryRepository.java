package com.stockpulse.repository;

import com.stockpulse.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByProductIdOrderByTimestampDesc(Long productId);
    List<PriceHistory> findByProductSkuOrderByTimestampDesc(String productSku);
    List<PriceHistory> findTop50ByOrderByTimestampDesc();
}
