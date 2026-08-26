package com.codify.sactionsservice.risk.config.dto;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.math.BigDecimal;

/**
 * No {@code ownerType} field — that's inferred by the mapper from nesting
 * position (an entry in {@code CategoryConfig.thresholds} is CATEGORY-owned;
 * an entry in {@code RiskModelConfig.overallThresholds} is MODEL-owned), not
 * declared redundantly by the tenant.
 */
public record ThresholdConfig(
        RiskLevel riskLevel,
        BigDecimal minScore,
        BigDecimal maxScore
) {
}
