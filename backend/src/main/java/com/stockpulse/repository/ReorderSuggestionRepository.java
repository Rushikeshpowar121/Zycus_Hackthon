package com.stockpulse.repository;

import com.stockpulse.entity.ReorderSuggestion;
import com.stockpulse.enums.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {
    List<ReorderSuggestion> findByProductIdOrderByCreatedAtDesc(Long productId);
    List<ReorderSuggestion> findByProductIdAndStatus(Long productId, SuggestionStatus status);
}
