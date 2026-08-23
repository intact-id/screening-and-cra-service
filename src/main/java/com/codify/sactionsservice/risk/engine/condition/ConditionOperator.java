package com.codify.sactionsservice.risk.engine.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public enum ConditionOperator {

    EQ("eq") {
        public boolean evaluate(Object actual, Object expected) {
            return Objects.equals(actual, expected);
        }
    },
    NE("ne") {
        public boolean evaluate(Object actual, Object expected) {
            return !Objects.equals(actual, expected);
        }
    },
    IN("in") {
        public boolean evaluate(Object actual, Object expected) {
            return expected instanceof Collection<?> set && set.contains(actual);
        }
    },
    NOT_IN("not_in") {
        public boolean evaluate(Object actual, Object expected) {
            return expected instanceof Collection<?> set && !set.contains(actual);
        }
    },
    GT("gt") {
        public boolean evaluate(Object actual, Object expected) {
            return compareNumeric(actual, expected) > 0;
        }
    },
    GTE("gte") {
        public boolean evaluate(Object actual, Object expected) {
            return compareNumeric(actual, expected) >= 0;
        }
    },
    LT("lt") {
        public boolean evaluate(Object actual, Object expected) {
            return compareNumeric(actual, expected) < 0;
        }
    },
    LTE("lte") {
        public boolean evaluate(Object actual, Object expected) {
            return compareNumeric(actual, expected) <= 0;
        }
    },
    EXISTS("exists") {
        public boolean evaluate(Object actual, Object expected) {
            return actual != null;
        }

        @Override
        public boolean evaluatesOnMissingAttribute() {
            return true;
        }
    };

    private final String wireValue;

    ConditionOperator(String wireValue) {
        this.wireValue = wireValue;
    }

    public abstract boolean evaluate(Object actual, Object expected);

    /**
     * Every operator evaluates to false when the attribute is absent from the
     * payload, except EXISTS, which is specifically checking for absence.
     */
    public boolean evaluatesOnMissingAttribute() {
        return false;
    }

    @JsonValue
    public String wireValue() {
        return wireValue;
    }

    @JsonCreator
    public static ConditionOperator fromWireValue(String wireValue) {
        for (ConditionOperator operator : values()) {
            if (operator.wireValue.equals(wireValue)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("Unknown condition operator: " + wireValue);
    }

    private static int compareNumeric(Object actual, Object expected) {
        return new BigDecimal(actual.toString()).compareTo(new BigDecimal(expected.toString()));
    }
}
