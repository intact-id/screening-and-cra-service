package com.codify.sactionsservice.risk.config.dto;

import com.codify.sactionsservice.risk.domain.overriderule.ForceLevel;

import java.util.Map;

public record OverrideRuleConfig(
        Map<String, Object> condition,
        ForceLevel forceLevel,
        String reason
) {
}
