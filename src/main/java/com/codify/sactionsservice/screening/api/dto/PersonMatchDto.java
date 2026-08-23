package com.codify.sactionsservice.screening.api.dto;

public record PersonMatchDto(
        String firstName,
        String lastName,
        String fatherName,
        String birthDate
) {
}
