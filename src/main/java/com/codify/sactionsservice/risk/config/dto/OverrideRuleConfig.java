package com.codify.sactionsservice.risk.config.dto;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.util.Map;

public record OverrideRuleConfig(
        Map<String, Object> condition,
        RiskLevel forceLevel,
        String reason
) {
}
