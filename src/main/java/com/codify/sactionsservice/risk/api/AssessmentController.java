package com.codify.sactionsservice.risk.api;

import com.codify.sactionsservice.common.ApiResult;
import com.codify.sactionsservice.common.web.ControllerUtil;
import com.codify.sactionsservice.risk.api.dto.AssessmentRequestDto;
import com.codify.sactionsservice.risk.api.dto.AssessmentResponseDto;
import com.codify.sactionsservice.risk.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/assessments", produces = "application/json")
public class AssessmentController extends ControllerUtil {

    private final AssessmentService assessmentService;

    @PostMapping
    public ResponseEntity<ApiResult<AssessmentResponseDto>> runAssessment(@RequestBody AssessmentRequestDto request) {
        return handleCreate(
                () -> assessmentService.runAssessment(request),
                log,
                "Assessment completed successfully",
                "no active risk model found while running assessment",
                "validation failed while running assessment",
                "failed to run assessment"
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<AssessmentResponseDto>> getAssessment(
            @PathVariable String id,
            @RequestParam(name = "include", required = false) String include) {
        boolean includeBreakdown = "breakdown".equalsIgnoreCase(include);

        return handleGet(
                () -> assessmentService.getAssessment(id, includeBreakdown),
                log,
                id,
                "Assessment fetched successfully",
                "Assessment not found",
                "failed to fetch assessment"
        );
    }
}
