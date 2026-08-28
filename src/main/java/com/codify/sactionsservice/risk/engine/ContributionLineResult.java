package com.codify.sactionsservice.risk.engine;

import java.math.BigDecimal;

public record ContributionLineResult(
        String categoryKey,
        String attributeKey,
        Object rawValue,
        Object normalizedValue,
        String matchedRuleId,
        Integer points,
        BigDecimal weightApplied,
        BigDecimal weightedPoints,
        String note
) {
}
