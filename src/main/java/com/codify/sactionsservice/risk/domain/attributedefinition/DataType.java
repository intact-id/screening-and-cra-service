package com.codify.sactionsservice.risk.domain.attributedefinition;

import lombok.Getter;

@Getter
public enum DataType {
    ENUM("A fixed set of string values"),
    NUMBER("A numeric value"),
    BOOLEAN("true / false"),
    CURRENCY_AMOUNT("A monetary amount, normalised to the tenant's base currency at scoring time"),
    DATE("A calendar date"),
    COUNTRY("An ISO country code"),
    STRING("Free-text value — usually scored via DEFAULT only, mainly for audit/display");

    private final String description;

    DataType(String description) {
        this.description = description;
    }
}
