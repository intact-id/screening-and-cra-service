package com.codify.sactionsservice.risk.config.dto;

import com.codify.sactionsservice.risk.domain.riskmodel.CombineMethod;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;

import java.util.List;

/**
 * The wire format for a tenant-submitted RiskModel config document.
 * Deliberately has no {@code version} field — version is system-assigned
 * (see RiskModelService.createDraft()), never accepted from tenant input.
 */
public record RiskModelConfig(
        CustomerType customerType,
        CombineMethod combineMethod,
        String description,
        List<ThresholdConfig> overallThresholds,
        List<OverrideRuleConfig> overrideRules,
        List<CategoryConfig> categories
) {
}
