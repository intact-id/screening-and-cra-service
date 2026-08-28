package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.attribute.MissingBehavior;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class AttributeScorer {

    private AttributeScorer() {
    }

    public static AttributeScoreResult score(AttributeSnapshot attribute, AttributePayload payload) {
        String key = attribute.attributeDefinitionKey();
        Object rawValue = payload.get(key);
        boolean present = payload.has(key) && Objects.nonNull(rawValue);

        if (present) {
            return matchRule(attribute, rawValue);
        }

        return applyMissingBehavior(attribute);
    }

    private static AttributeScoreResult matchRule(AttributeSnapshot attribute, Object rawValue) {
        List<ValueScoreRuleSnapshot> sortedRules = attribute.rules().stream()
                .sorted(Comparator.comparing(ValueScoreRuleSnapshot::priority))
                .toList();

        for (ValueScoreRuleSnapshot rule : sortedRules) {
            if (matches(rule, rawValue)) {
                return new AttributeScoreResult(rule.score(), rule.id(), false, null);
            }
        }

        throw new IllegalStateException(
                "No ValueScoreRule matched attribute '" + attribute.attributeDefinitionKey()
                        + "' for value '" + rawValue + "' and no DEFAULT rule was configured");
    }

    private static boolean matches(ValueScoreRuleSnapshot rule, Object rawValue) {
        return switch (rule.matchType()) {
            case EXACT, BOOLEAN -> Objects.equals(rawValue, rule.matchValue().get("eq"));
            case SET -> rule.matchValue().get("in") instanceof List<?> options && options.contains(rawValue);
            case RANGE -> inRange(rawValue, rule.matchValue().get("min"), rule.matchValue().get("max"));
            case DEFAULT -> true;
        };
    }

    private static boolean inRange(Object rawValue, Object min, Object max) {
        BigDecimal value = toBigDecimal(rawValue);
        return value.compareTo(toBigDecimal(min)) >= 0 && value.compareTo(toBigDecimal(max)) <= 0;
    }

    private static BigDecimal toBigDecimal(Object value) {
        return new BigDecimal(value.toString());
    }

    private static AttributeScoreResult applyMissingBehavior(AttributeSnapshot attribute) {
        if (attribute.required() && attribute.missingBehavior() == MissingBehavior.DEFAULT_SCORE) {
            throw new IllegalStateException(
                    "Attribute '" + attribute.attributeDefinitionKey()
                            + "' is required but configured with missingBehavior=DEFAULT_SCORE — this must never reach the scorer");
        }

        return switch (attribute.missingBehavior()) {
            case DEFAULT_SCORE -> new AttributeScoreResult(attribute.defaultScore(), null, false,
                    "value missing → DEFAULT_SCORE applied");
            case MAX_RISK -> new AttributeScoreResult(100, null, false, "value missing → MAX_RISK applied");
            case INCOMPLETE -> new AttributeScoreResult(100, null, true, "value missing → INCOMPLETE (required)");
        };
    }
}
