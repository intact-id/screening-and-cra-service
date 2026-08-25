package com.codify.sactionsservice.risk.domain.attribute;

import lombok.Getter;

@Getter
public enum MissingBehavior {
    DEFAULT_SCORE("Use a fallback score — fine for optional attributes"),
    MAX_RISK("Treat absence as the worst case — appropriate for required identity/country fields"),
    INCOMPLETE("Mark the whole assessment incomplete and require resolution");

    private final String description;

    MissingBehavior(String description) {
        this.description = description;
    }
}
