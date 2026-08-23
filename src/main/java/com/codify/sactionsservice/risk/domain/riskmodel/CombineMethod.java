package com.codify.sactionsservice.risk.domain.riskmodel;

import lombok.Getter;

@Getter
public enum CombineMethod {
    WEIGHTED_SUM("Base score is a weighted sum of category scores; no override rules applied."),
    MAX("Overall level is the highest individual category level; ignores weights and scores."),
    HYBRID("Weighted sum for the base score/level, then override rules can raise it. Recommended default.");

    private final String description;

    CombineMethod(String description) {
        this.description = description;
    }
}
