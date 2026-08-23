package com.codify.sactionsservice.risk.domain.threshold;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class ThresholdOwnerValidator {

    @PrePersist
    @PreUpdate
    public void validateOwner(Threshold threshold) {
        boolean hasCategory = threshold.getCategory() != null;
        boolean hasRiskModel = threshold.getRiskModel() != null;

        if (hasCategory == hasRiskModel) {
            throw new IllegalStateException("Threshold must have exactly one owner (category or riskModel)");
        }
        if ((threshold.getOwnerType() == OwnerType.CATEGORY) != hasCategory) {
            throw new IllegalStateException("ownerType does not match the populated owner");
        }
    }
}
