package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.math.BigDecimal;
import java.util.List;

public record ScoringResult(
        BigDecimal overallScore,
        RiskLevel overallLevel,
        boolean overrideApplied,
        List<CategoryScoreResult> categoryResults,
        List<ContributionLineResult> contributionLines,
        List<OverrideEvaluationResult> overrideEvaluations
) {
}
