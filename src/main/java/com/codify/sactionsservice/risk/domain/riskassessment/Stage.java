package com.codify.sactionsservice.risk.domain.riskassessment;

import lombok.Getter;

@Getter
public enum Stage {
    ONBOARDING("Runs before/during onboarding — only ONBOARDING-timing categories are scored; Screening is excluded"),
    POST_ONBOARDING("Runs after the onboarding case has closed — all categories are scored, including Screening");

    private final String description;

    Stage(String description) {
        this.description = description;
    }
}
