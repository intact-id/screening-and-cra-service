package com.codify.sactionsservice.risk.domain.overriderule;

import com.codify.sactionsservice.risk.domain.RiskLevel;

public enum ForceLevel {
    MEDIUM(RiskLevel.MEDIUM),
    HIGH(RiskLevel.HIGH);

    private final RiskLevel riskLevel;

    ForceLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public RiskLevel toRiskLevel() {
        return riskLevel;
    }
}
