package com.codify.sactionsservice.risk.api;

import com.codify.sactionsservice.common.ApiResult;
import com.codify.sactionsservice.common.web.ControllerUtil;
import com.codify.sactionsservice.risk.api.dto.RiskModelDetailResponseDto;
import com.codify.sactionsservice.risk.api.dto.RiskModelResponseDto;
import com.codify.sactionsservice.risk.api.dto.ValidationResponseDto;
import com.codify.sactionsservice.risk.config.dto.RiskModelConfig;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.domain.riskmodel.LifeCycleStatus;
import com.codify.sactionsservice.risk.service.RiskModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/risk-models", produces = "application/json")
public class RiskModelController extends ControllerUtil {

    private final RiskModelService riskModelService;

    @PostMapping
    public ResponseEntity<ApiResult<RiskModelResponseDto>> createDraft(
            @RequestParam String tenantId,
            @RequestBody RiskModelConfig config) {
        return handleCreate(
                () -> riskModelService.createDraft(tenantId, config),
                log,
                "Risk model draft created successfully",
                "tenant not found while creating risk model draft",
                "validation failed while creating risk model draft",
                "failed to create risk model draft"
        );
    }

    @GetMapping
    public ResponseEntity<ApiResult<List<RiskModelResponseDto>>> list(
            @RequestParam String tenantId,
            @RequestParam(required = false) CustomerType customerType,
            @RequestParam(required = false) LifeCycleStatus status) {
        return handleGet(
                () -> riskModelService.list(tenantId, customerType, status),
                log,
                tenantId,
                "Risk models fetched successfully",
                "tenant not found while listing risk models: tenantId={} reason={}",
                "failed to list risk models: tenantId={} reason={}"
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResult<RiskModelDetailResponseDto>> getActive(
            @RequestParam String tenantId,
            @RequestParam CustomerType customerType) {
        return handleGet(
                () -> riskModelService.getActiveConfig(tenantId, customerType),
                log,
                tenantId,
                "Active risk model fetched successfully",
                "no active risk model: tenantId={} reason={}",
                "failed to fetch active risk model: tenantId={} reason={}"
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<RiskModelDetailResponseDto>> getById(@PathVariable String id) {
        return handleGet(
                () -> riskModelService.getById(id),
                log,
                id,
                "Risk model fetched successfully",
                "risk model not found: riskModelId={} reason={}",
                "failed to fetch risk model: riskModelId={} reason={}"
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResult<RiskModelDetailResponseDto>> updateDraft(
            @PathVariable String id,
            @RequestBody RiskModelConfig config) {
        return handleUpdate(
                () -> riskModelService.updateDraft(id, config),
                log,
                id,
                "Risk model draft updated successfully",
                "risk model not found for update: riskModelId={} reason={}",
                "update rejected for risk model: riskModelId={} reason={}",
                "failed to update risk model: riskModelId={} reason={}"
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<RiskModelResponseDto>> deleteDraft(@PathVariable String id) {
        return handleUpdate(
                () -> riskModelService.deleteDraft(id),
                log,
                id,
                "Risk model draft deleted successfully",
                "risk model not found for delete: riskModelId={} reason={}",
                "delete rejected for risk model: riskModelId={} reason={}",
                "failed to delete risk model: riskModelId={} reason={}"
        );
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResult<RiskModelResponseDto>> cloneToDraft(@PathVariable String id) {
        return handleCreate(
                () -> riskModelService.cloneToDraft(id),
                log,
                "Risk model cloned into a new draft successfully",
                "risk model not found for clone: {}",
                "clone rejected: {}",
                "failed to clone risk model: {}"
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResult<ValidationResponseDto>> validate(
            @RequestParam(defaultValue = "false") boolean forActivation,
            @RequestBody RiskModelConfig config) {
        return handleGet(
                () -> riskModelService.validate(config, forActivation),
                log,
                String.valueOf(forActivation),
                "Risk model configuration validated",
                "validation lookup failed: forActivation={} reason={}",
                "failed to validate risk model configuration: forActivation={} reason={}"
        );
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResult<RiskModelResponseDto>> activate(@PathVariable String id) {
        return handleUpdate(
                () -> riskModelService.activate(id),
                log,
                id,
                "Risk model activated successfully",
                "Risk model not found for activation",
                "validation failed while activating risk model",
                "failed to activate risk model"
        );
    }
}
