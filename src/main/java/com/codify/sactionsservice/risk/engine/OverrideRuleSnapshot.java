package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.util.Map;

public record OverrideRuleSnapshot(
        String id,
        Map<String, Object> condition,
        RiskLevel forceLevel,
        String reason
) {
}
