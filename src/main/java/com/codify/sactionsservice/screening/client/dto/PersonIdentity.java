package com.codify.sactionsservice.screening.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonIdentity(
        String firstName,
        String lastName,
        String fatherName,
        String birthDate
) implements Identity {

}
