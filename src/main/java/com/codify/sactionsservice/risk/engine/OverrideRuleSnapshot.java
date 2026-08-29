package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.overriderule.ForceLevel;

import java.util.Map;

public record OverrideRuleSnapshot(
        String id,
        Map<String, Object> condition,
        ForceLevel forceLevel,
        String reason
) {
}
