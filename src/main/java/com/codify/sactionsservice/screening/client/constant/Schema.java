package com.codify.sactionsservice.screening.client.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Schema {
    PERSON("Person"),
    BUSINESS("Company");

    @JsonValue
    private final String q;

    Schema(String q) {
        this.q = q;
    }

}
