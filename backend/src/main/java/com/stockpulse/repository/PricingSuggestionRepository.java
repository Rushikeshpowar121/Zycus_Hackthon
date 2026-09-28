package com.stockpulse.repository;

import com.stockpulse.entity.PricingSuggestion;
import com.stockpulse.enums.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingSuggestionRepository extends JpaRepository<PricingSuggestion, Long> {
    List<PricingSuggestion> findByProductIdOrderByCreatedAtDesc(Long productId);
    List<PricingSuggestion> findByProductIdAndStatus(Long productId, SuggestionStatus status);
}
