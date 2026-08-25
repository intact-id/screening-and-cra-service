package com.codify.sactionsservice.risk.domain.attribute.valuescorerule;

import lombok.Getter;

@Getter
public enum MatchType {
    EXACT("Value must equal a single specified value — matchValue: {\"eq\": ...}"),
    RANGE("Value must fall within an inclusive min/max — matchValue: {\"min\": ..., \"max\": ...}"),
    SET("Value must be one of a list — matchValue: {\"in\": [...]}"),
    BOOLEAN("Value must equal true/false — matchValue: {\"eq\": true|false}"),
    DEFAULT("Always matches — the fallback when nothing else matched");

    private final String description;

    MatchType(String description) {
        this.description = description;
    }
}

