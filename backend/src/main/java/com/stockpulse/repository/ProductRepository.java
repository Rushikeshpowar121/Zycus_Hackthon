package com.stockpulse.repository;

import com.stockpulse.entity.Product;
import com.stockpulse.enums.InventoryStatus;
import com.stockpulse.enums.PricingStrategyType;
import com.stockpulse.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByCategory(String category);
    List<Product> findByStatus(InventoryStatus status);
    List<Product> findByRiskLevel(RiskLevel riskLevel);
    List<Product> findByActiveStrategyType(PricingStrategyType strategyType);

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findAllCategories();

    long countByStatus(InventoryStatus status);
    long countByRiskLevel(RiskLevel riskLevel);
}
