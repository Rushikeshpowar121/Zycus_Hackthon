package com.stockpulse.service;

import com.stockpulse.entity.SystemConfiguration;
import com.stockpulse.enums.AdvisorType;
import com.stockpulse.repository.SystemConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AdvisorRegistry {

    private final CommerceAdvisor ruleBasedAdvisor;
    private final CommerceAdvisor aiAdvisor;
    private final CommerceAdvisor competitorAwareAdvisor;
    private final SystemConfigurationRepository configRepository;

    public AdvisorRegistry(
            @Qualifier("ruleBasedAdvisor") CommerceAdvisor ruleBasedAdvisor,
            @Qualifier("aiAdvisor") CommerceAdvisor aiAdvisor,
            @Qualifier("competitorAwareAdvisor") CommerceAdvisor competitorAwareAdvisor,
            SystemConfigurationRepository configRepository) {
        this.ruleBasedAdvisor = ruleBasedAdvisor;
        this.aiAdvisor = aiAdvisor;
        this.competitorAwareAdvisor = competitorAwareAdvisor;
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public CommerceAdvisor getActiveAdvisor() {
        AdvisorType activeType = getActiveAdvisorType();
        log.info("AdvisorRegistry: Active advisor selected at runtime -> {}", activeType);

        switch (activeType) {
            case AI:
                return aiAdvisor;
            case COMPETITOR:
                return competitorAwareAdvisor;
            case RULE:
            default:
                return ruleBasedAdvisor;
        }
    }

    @Transactional(readOnly = true)
    public AdvisorType getActiveAdvisorType() {
        return configRepository.findTopByOrderByIdAsc()
                .map(SystemConfiguration::getActiveAdvisor)
                .orElseGet(() -> {
                    // Default to RULE if unconfigured
                    configRepository.save(SystemConfiguration.builder()
                            .activeAdvisor(AdvisorType.RULE)
                            .build());
                    return AdvisorType.RULE;
                });
    }

    @Transactional
    public SystemConfiguration setActiveAdvisorType(AdvisorType type) {
        if (type == null) {
            throw new IllegalArgumentException("AdvisorType cannot be null");
        }

        SystemConfiguration config = configRepository.findTopByOrderByIdAsc()
                .orElse(SystemConfiguration.builder().build());

        config.setActiveAdvisor(type);
        SystemConfiguration saved = configRepository.save(config);
        log.info("AdvisorRegistry: Successfully updated active advisor configuration to {} at runtime without restart.", type);
        return saved;
    }
}
