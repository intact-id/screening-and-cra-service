package com.codify.sactionsservice.risk.config.dto;

import com.codify.sactionsservice.risk.domain.attribute.MissingBehavior;

import java.math.BigDecimal;
import java.util.List;

public record AttributeConfig(
        String attributeDefinitionKey,
        BigDecimal weight,
        Boolean required,
        MissingBehavior missingBehavior,
        Integer defaultScore,
        List<ValueScoreRuleConfig> rules
) {
}
