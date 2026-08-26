package com.codify.sactionsservice.risk.config.dto;

import com.codify.sactionsservice.risk.domain.attribute.valuescorerule.MatchType;

import java.util.Map;

public record ValueScoreRuleConfig(
        MatchType matchType,
        Map<String, Object> matchValue,
        Integer score,
        Integer priority
) {
}
