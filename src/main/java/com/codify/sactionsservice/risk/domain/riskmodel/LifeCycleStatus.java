package com.codify.sactionsservice.risk.domain.riskmodel;

import lombok.Getter;

@Getter
public enum LifeCycleStatus {
    DRAFT(0, "Being authored/edited. Not used to score anything yet."),
    ACTIVE(1, "The live version, one active version per (tenant, customerType) at a time."),
    RETIRED(2, "Superseded — was ACTIVE once, got replaced when a newer draft was activated.");

    private final int rank;
    private final String description;

    LifeCycleStatus(int rank, String description) {
        this.rank = rank;
        this.description = description;
    }
}
