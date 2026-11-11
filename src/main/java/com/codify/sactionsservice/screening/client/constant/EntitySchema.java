package com.codify.sactionsservice.screening.client.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/** FollowTheMoney entity types a search can be limited to. */
@Getter
public enum EntitySchema {
    THING("Thing"),
    LEGAL_ENTITY("LegalEntity"),
    PERSON("Person"),
    ORGANIZATION("Organization"),
    COMPANY("Company"),
    PUBLIC_BODY("PublicBody"),
    VESSEL("Vessel"),
    AIRPLANE("Airplane"),
    CRYPTO_WALLET("CryptoWallet"),
    SECURITY("Security");

    /** The name the engines expect, case-sensitive. */
    @JsonValue
    private final String value;

    EntitySchema(String value) {
        this.value = value;
    }

    /** Accepts the constant name or the engine value in any case: PERSON, person, Person. */
    public static EntitySchema from(String text) {
        String t = text.trim();
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(t) || s.value.equalsIgnoreCase(t))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown schema: " + text));
    }
}
