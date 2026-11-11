package com.codify.sactionsservice.risk.api.dto;

import com.codify.sactionsservice.common.FieldError;

import java.util.List;

public record ValidationResponseDto(
        boolean valid,
        List<FieldError> errors,
        List<FieldError> warnings
) {
}
