package com.codify.sactionsservice.tenant.dto;


import com.codify.sactionsservice.tenant.TenantType;
import com.codify.sactionsservice.tenant.Tenant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record TenantCreationDto(
        String tenantIdentifier,

        @NotNull(message = "Tenant type is required")
        TenantType tenantType,

        @NotBlank(message = "Tenant name cannot be blank")
        @Size(max = 200, message = "Tenant name must not exceed 255 characters")
        String tenantName,

        @Size(max = 500, message = "Tenant description cannot exceed 500 characters")
        String description,

        Map<String, Object> extraDetails
) {

    public Tenant toTenant() {
        return Tenant.builder()
                .tenantIdentifier(tenantIdentifier)
                .tenantType(tenantType)
                .tenantName(tenantName)
                .description(description)
                .extraDetails(extraDetails)
                .isActive(true)
                .build();
    }

}
