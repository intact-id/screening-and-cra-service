package com.codify.sactionsservice.tenant.api.dto;

import com.codify.sactionsservice.tenant.domain.TenantType;

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
