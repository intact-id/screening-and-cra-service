package com.codify.sactionsservice.risk.domain.riskassessment;

import lombok.Getter;

@Getter
public enum TriggeredBy {
    ONBOARDING("Run as part of customer onboarding"),
    SCREENING_EVENT("Run because a new sanctions/PEP/adverse-media hit came in"),
    PERIODIC("Run as part of scheduled ongoing monitoring"),
    MANUAL("Run on demand by an operator");

    private final String description;

    TriggeredBy(String description) {
        this.description = description;
    }
}
