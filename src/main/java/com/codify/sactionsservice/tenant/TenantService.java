package com.codify.sactionsservice.tenant;

import com.codify.sactionsservice.tenant.dto.TenantCreationDto;
import com.codify.sactionsservice.tenant.dto.TenantResponseDto;
import com.codify.sactionsservice.tenant.dto.TenantUpdateRequestDto;
import com.codify.sactionsservice.common.exception.ApplicationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@AllArgsConstructor
public class TenantService {
    private final TenantRepo tenantRepo;


    public TenantResponseDto createTenant(TenantCreationDto tenantCreationDto) {
        if (Boolean.TRUE.equals(existByTenantIdentifier(tenantCreationDto.tenantIdentifier()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant already exists");
        }
        var response = tenantRepo.save(tenantCreationDto.toTenant());
        log.info("Created Tenant: {} with id: {}", response.getTenantName(), response.getId());

        return response.toDto();

    }

    public TenantResponseDto findById(String id) {
        log.info("Fetching Tenant by id: {}", id);
        return  tenantRepo.findById(id)
                .map(Tenant::toDto)
                .orElseThrow(() -> new ApplicationException.ResourceNotFoundException("Tenant not found"));
    }

    public TenantResponseDto updateTenant(String id, TenantUpdateRequestDto tenantUpdateRequestDto) {
        log.info("Updating Tenant with id: {}", id);
        Tenant tenant = tenantRepo.findById(id)
                .orElseThrow(() -> new ApplicationException.ResourceNotFoundException("Tenant not found"));

        if (tenantUpdateRequestDto.tenantIdentifier() != null) {
            tenant.setTenantIdentifier(tenantUpdateRequestDto.tenantIdentifier());
        }
        if (tenantUpdateRequestDto.tenantType() != null) {
            tenant.setTenantType(tenantUpdateRequestDto.tenantType());
        }
        if (tenantUpdateRequestDto.tenantName() != null) {
            tenant.setTenantName(tenantUpdateRequestDto.tenantName());
        }
        if (tenantUpdateRequestDto.description() != null) {
            tenant.setDescription(tenantUpdateRequestDto.description());
        }
        if (tenantUpdateRequestDto.isActive() != null) {
            tenant.setIsActive(tenantUpdateRequestDto.isActive());
        }
        if (tenantUpdateRequestDto.extraDetails() != null) {
            tenant.setExtraDetails(tenantUpdateRequestDto.extraDetails());
        }

        var response = tenantRepo.save(tenant);
        log.info("Updated Tenant: {} with id: {}", response.getTenantName(), response.getId());

        return response.toDto();
    }


    private Boolean existByTenantIdentifier(String tenantIdentifier) {
        return null;
    }
}
