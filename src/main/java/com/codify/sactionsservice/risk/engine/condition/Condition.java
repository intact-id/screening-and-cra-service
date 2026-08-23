package com.codify.sactionsservice.risk.engine.condition;

import java.util.List;

public sealed interface Condition permits Condition.Leaf, Condition.All, Condition.Any {

    record Leaf(
            String attribute,
            ConditionOperator operator,
            Object value) implements Condition {

    }


    record All(List<Condition> conditions) implements Condition {
    }

    record Any(List<Condition> conditions) implements Condition {
    }
}
