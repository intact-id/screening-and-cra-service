package com.codify.sactionsservice.risk.config.validator;

import java.util.List;

public record ValidationResult(List<ValidationError> errors, List<ValidationWarning> warnings) {

    public boolean isValid() {
        return errors.isEmpty();
    }
}
