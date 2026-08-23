package com.codify.sactionsservice.tenant.domain;

public enum TenantType {
    SERVICE("This identifies internal services"),
    ENTITY("This identifies external services/entities");
    private String value;

    TenantType(String value) {
    }
}
