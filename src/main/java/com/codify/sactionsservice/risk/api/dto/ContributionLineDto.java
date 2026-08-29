package com.codify.sactionsservice.risk.api.dto;

import java.math.BigDecimal;

public record ContributionLineDto(
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
