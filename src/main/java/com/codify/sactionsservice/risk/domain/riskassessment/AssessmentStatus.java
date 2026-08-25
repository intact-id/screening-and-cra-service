package com.codify.sactionsservice.risk.domain.riskassessment;

import lombok.Getter;

@Getter
public enum AssessmentStatus {
    COMPLETE("Every required attribute was scored; overallScore/overallLevel are final"),
    INCOMPLETE("At least one required attribute was missing (missingBehavior: INCOMPLETE) — needs resolution before this assessment can be trusted");

    private final String description;

    AssessmentStatus(String description) {
        this.description = description;
    }
}
