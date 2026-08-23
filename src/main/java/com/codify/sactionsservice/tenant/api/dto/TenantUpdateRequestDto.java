package com.codify.sactionsservice.tenant.api.dto;

import com.codify.sactionsservice.tenant.domain.TenantType;
import jakarta.validation.constraints.Size;

import java.util.Map;


public record TenantUpdateRequestDto(
        String tenantIdentifier,

        TenantType tenantType,

        @Size(max = 200, message = "Tenant name must not exceed 200 characters")
        String tenantName,

        @Size(max = 500, message = "Tenant description cannot exceed 500 characters")
        String description,

        Boolean isActive,

        Map<String, Object> extraDetails
) {
}