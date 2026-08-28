package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.RiskLevel;

public record OverrideEvaluationResult(String overrideRuleId, boolean triggered, RiskLevel forceLevel) {
}
