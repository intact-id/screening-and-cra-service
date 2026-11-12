package com.codify.sactionsservice.screening.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * FollowTheMoney properties are multi-valued, so each one is sent as a list.
 * yente accepts plain strings too, but motiva only accepts lists.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PersonIdentity(
        List<String> firstName,
        List<String> lastName,
        List<String> fatherName,
        List<String> birthDate
) implements Identity {

    public static PersonIdentity of(String firstName, String lastName, String fatherName, String birthDate) {
        return new PersonIdentity(
                Identity.values(firstName),
                Identity.values(lastName),
                Identity.values(fatherName),
                Identity.values(birthDate)
        );
    }
}
