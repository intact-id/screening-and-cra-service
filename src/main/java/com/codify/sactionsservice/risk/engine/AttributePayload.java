package com.codify.sactionsservice.risk.engine;

import java.util.Map;

public record AttributePayload(Map<String, Object> values) {

    public Object get(String attributeKey) {
        return values.get(attributeKey);
    }

    public boolean has(String attributeKey) {
        return values.containsKey(attributeKey);
    }
}
