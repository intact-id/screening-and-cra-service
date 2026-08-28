package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.attribute.MissingBehavior;

import java.math.BigDecimal;
import java.util.List;

public record AttributeSnapshot(
        String attributeDefinitionKey,
        BigDecimal weight,
        boolean required,
        MissingBehavior missingBehavior,
        Integer defaultScore,
        List<ValueScoreRuleSnapshot> rules
) {
}
