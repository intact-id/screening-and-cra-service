package com.codify.sactionsservice.risk.api;

import com.codify.sactionsservice.common.ApiResult;
import com.codify.sactionsservice.common.web.ControllerUtil;
import com.codify.sactionsservice.risk.api.dto.RiskModelResponseDto;
import com.codify.sactionsservice.risk.config.dto.RiskModelConfig;
import com.codify.sactionsservice.risk.service.RiskModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
