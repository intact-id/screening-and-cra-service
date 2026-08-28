package com.codify.sactionsservice.risk.engine.condition;

import java.util.List;
import java.util.Map;

public final class ConditionParser {

    private ConditionParser() {
    }

    public static Condition parse(Map<String, Object> raw) {
        Object attribute = raw.get("attribute");
        if (attribute instanceof String key) {
            var operator = ConditionOperator.fromWireValue((String) raw.get("op"));
            return new Condition.Leaf(key, operator, raw.get("value"));
        }

        if (raw.get("all") instanceof List<?> children) {
            return new Condition.All(parseChildren(children));
        }

        if (raw.get("any") instanceof List<?> children) {
            return new Condition.Any(parseChildren(children));
        }

        throw new IllegalArgumentException(
                "Condition is neither a leaf (attribute/op/value) nor a composite (all/any): " + raw);
    }

    @SuppressWarnings("unchecked")
    private static List<Condition> parseChildren(List<?> children) {
        return children.stream()
                .map(child -> parse((Map<String, Object>) child))
                .toList();
    }
}
