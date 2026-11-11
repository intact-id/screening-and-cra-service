package com.codify.sactionsservice.screening.client.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/** OpenSanctions risk topics an entity can be tagged with. */
@Getter
public enum Topic {
    SANCTION("sanction"),
    SANCTION_LINKED("sanction.linked"),
    SANCTION_COUNTER("sanction.counter"),
    DEBARMENT("debarment"),
    ASSET_FROZEN("asset.frozen"),
    EXPORT_CONTROL("export.control"),
    ROLE_PEP("role.pep"),
    ROLE_RCA("role.rca"),
    ROLE_POL("role.pol"),
    ROLE_JUDGE("role.judge"),
    ROLE_CIVIL("role.civil"),
    ROLE_DIPLO("role.diplo"),
    ROLE_OLIGARCH("role.oligarch"),
    GOV_SOE("gov.soe"),
    CRIME("crime"),
    CRIME_FRAUD("crime.fraud"),
    CRIME_FIN("crime.fin"),
    CRIME_TERROR("crime.terror"),
    CRIME_TRAFFICK("crime.traffick"),
    CRIME_WAR("crime.war"),
    CRIME_CYBER("crime.cyber"),
    WANTED("wanted"),
    POI("poi"),
    REG_ACTION("reg.action"),
    REG_WARN("reg.warn"),
    CORP_SHELL("corp.shell"),
    CORP_OFFSHORE("corp.offshore"),
    CORP_DISQUAL("corp.disqual");

    /** The topic id the engines use. */
    @JsonValue
    private final String value;

    Topic(String value) {
        this.value = value;
    }

    /** Accepts the constant name or the topic id in any case: ROLE_PEP, role.pep. */
    public static Topic from(String text) {
        String t = text.trim();
        return Arrays.stream(values())
                .filter(topic -> topic.name().equalsIgnoreCase(t) || topic.value.equalsIgnoreCase(t))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown topic: " + text));
    }
}
