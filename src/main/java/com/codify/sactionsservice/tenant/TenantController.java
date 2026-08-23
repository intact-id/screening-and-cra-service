package com.codify.sactionsservice.tenant;

import com.codify.sactionsservice.common.ApiResult;
import com.codify.sactionsservice.tenant.dto.TenantCreationDto;
import com.codify.sactionsservice.tenant.dto.TenantResponseDto;
import com.codify.sactionsservice.tenant.dto.TenantUpdateRequestDto;
import com.codify.sactionsservice.common.web.ControllerUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/tenant", produces = "application/json")
public class TenantController extends ControllerUtil {
    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<ApiResult<TenantResponseDto>> createTenant(@Valid @RequestBody TenantCreationDto tenantCreationDto) {
        return handleCreate(
                () -> tenantService.createTenant(tenantCreationDto),
                log,
                "Tenant created successfully",
                "validation failed",
                "failed to create tenant"
        );
    }

    @GetMapping
    public ResponseEntity<ApiResult<TenantResponseDto>> getTenantById(@RequestParam String id) {
        return handleGet(
                () -> tenantService.findById(id),
                log,
                id,
                "Tenant fetched successfully",
                "Validation failed when fetching tenant",
                "failed to fetch tenant"
        );
    }

    @PatchMapping
    public ResponseEntity<ApiResult<TenantResponseDto>> updateTenant(@RequestParam String id, @Valid @RequestBody TenantUpdateRequestDto tenantUpdateRequestDto) {
        return handleUpdate(
                () -> tenantService.updateTenant(id, tenantUpdateRequestDto),
                log,
                id,
                "Tenant updated successfully",
                "Tenant not found for update",
                "validation failed when updating tenant",
                "failed to update tenant"
        );
    }

}
