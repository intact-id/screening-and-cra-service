package com.codify.sactionsservice.screening.client.dto;

import java.util.List;

public sealed interface Identity permits PersonIdentity, BusinessIdentity {

    /** Wraps a single value as a FollowTheMoney property list; blank values are dropped. */
    static List<String> values(String value) {
        return value == null || value.isBlank() ? List.of() : List.of(value.trim());
    }
}
