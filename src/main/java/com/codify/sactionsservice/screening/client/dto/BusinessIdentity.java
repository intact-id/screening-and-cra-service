package com.codify.sactionsservice.screening.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * FollowTheMoney properties are multi-valued, so each one is sent as a list.
 * yente accepts plain strings too, but motiva only accepts lists.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record BusinessIdentity(
        List<String> name,
        List<String> jurisdiction
) implements Identity {

    public static BusinessIdentity of(String name, String jurisdiction) {
        return new BusinessIdentity(Identity.values(name), Identity.values(jurisdiction));
    }
}
