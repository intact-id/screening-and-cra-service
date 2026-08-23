package com.codify.sactionsservice.tenant.dto;

import com.codify.sactionsservice.tenant.TenantType;

import java.util.Map;

public record TenantResponseDto(
        String tenantIdentifier,
        TenantType tenantType,
        String tenantName,
        String description,
        Map<String, Object> extraDetails,
        Boolean isActive,
        String id
) {

}
