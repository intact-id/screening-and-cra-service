package com.codify.sactionsservice.risk.domain.attributedefinition;

import lombok.Getter;

@Getter
public enum SourceSystem {
    MANUAL("Entered directly by an operator"),
    ID_VERIFICATION("Derived from identity document verification"),
    BIOMETRIC("Derived from the liveness/face-match step"),
    REGISTRY("Derived from a business/company registry lookup, e.g. CR12"),
    SCREENING("Derived from sanctions/PEP/adverse-media screening");

    private final String description;

    SourceSystem(String description) {
        this.description = description;
    }
}