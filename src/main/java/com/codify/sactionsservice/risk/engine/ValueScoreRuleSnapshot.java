package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.attribute.valuescorerule.MatchType;

import java.util.Map;

public record ValueScoreRuleSnapshot(
        String id,
        MatchType matchType,
        Map<String, Object> matchValue,
        Integer priority,
        Integer score
) {
}
