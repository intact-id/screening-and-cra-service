package com.codify.sactionsservice.risk.api.dto;

import com.codify.sactionsservice.risk.domain.RiskLevel;
import com.codify.sactionsservice.risk.domain.riskassessment.AssessmentStatus;
import com.codify.sactionsservice.risk.domain.riskassessment.Stage;
import com.codify.sactionsservice.risk.domain.riskassessment.TriggeredBy;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;

import java.math.BigDecimal;
import java.util.List;

public record AssessmentResponseDto(
        String id,
        String tenantId,
        String customerId,
        CustomerType customerType,
        String riskModelId,
        Integer modelVersion,
        Stage stage,
        AssessmentStatus status,
        BigDecimal overallScore,
        RiskLevel overallLevel,
        Boolean overrideApplied,
        TriggeredBy triggeredBy,
        ScreeningResult screening,
        List<CategoryResultDto> categoryResults,
        List<ContributionLineDto> contributionLines
) {
}
