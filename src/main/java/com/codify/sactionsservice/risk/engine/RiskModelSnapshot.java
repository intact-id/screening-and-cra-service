package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.riskmodel.CombineMethod;

import java.util.List;

public record RiskModelSnapshot(
        String riskModelId,
        Integer version,
        CombineMethod combineMethod,
        List<ThresholdBand> overallThresholds,
        List<OverrideRuleSnapshot> overrideRules,
        List<CategorySnapshot> categories
) {
}
