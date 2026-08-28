package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.engine.condition.Condition;
import com.codify.sactionsservice.risk.engine.condition.ConditionParser;

import java.util.Map;

public final class OverrideEvaluator {

    private OverrideEvaluator() {
    }

    public static boolean evaluate(Map<String, Object> rawCondition, AttributePayload payload) {
        return evaluate(ConditionParser.parse(rawCondition), payload);
    }

    private static boolean evaluate(Condition condition, AttributePayload payload) {
        return switch (condition) {
            case Condition.Leaf leaf -> evaluateLeaf(leaf, payload);
            case Condition.All all -> all.conditions().stream().allMatch(child -> evaluate(child, payload));
            case Condition.Any any -> any.conditions().stream().anyMatch(child -> evaluate(child, payload));
        };
    }

    private static boolean evaluateLeaf(Condition.Leaf leaf, AttributePayload payload) {
        boolean present = payload.has(leaf.attribute()) && payload.get(leaf.attribute()) != null;

        if (!present && !leaf.operator().evaluatesOnMissingAttribute()) {
            return false;
        }

        Object actual = present ? payload.get(leaf.attribute()) : null;
        return leaf.operator().evaluate(actual, leaf.value());
    }
}
