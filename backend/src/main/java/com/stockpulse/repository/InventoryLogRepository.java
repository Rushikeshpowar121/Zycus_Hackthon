package com.stockpulse.repository;

import com.stockpulse.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    List<InventoryLog> findByProductIdOrderByTimestampDesc(Long productId);
    List<InventoryLog> findTop50ByOrderByTimestampDesc();
}
