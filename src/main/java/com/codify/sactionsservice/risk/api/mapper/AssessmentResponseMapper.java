package com.codify.sactionsservice.risk.api.mapper;

import com.codify.sactionsservice.risk.api.dto.AssessmentResponseDto;
import com.codify.sactionsservice.risk.api.dto.CategoryResultDto;
import com.codify.sactionsservice.risk.api.dto.ContributionLineDto;
import com.codify.sactionsservice.risk.domain.categoryresult.CategoryResult;
import com.codify.sactionsservice.risk.domain.contributionline.ContributionLine;
import com.codify.sactionsservice.risk.domain.riskassessment.RiskAssessment;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AssessmentResponseMapper {

    private final ObjectMapper objectMapper;

    public AssessmentResponseDto toDto(RiskAssessment assessment, boolean includeBreakdown) {
        return new AssessmentResponseDto(
                assessment.getId(),
                assessment.getTenant().getId(),
                assessment.getCustomerId(),
                assessment.getCustomerType(),
                assessment.getRiskModel().getId(),
                assessment.getModelVersion(),
                assessment.getStage(),
                assessment.getStatus(),
                assessment.getOverallScore(),
                assessment.getOverallLevel(),
                assessment.getOverrideApplied(),
                assessment.getTriggeredBy(),
                mapScreeningResult(assessment.getScreeningSnapshot()),
                mapCategoryResults(assessment.getCategoryResults()),
                includeBreakdown ? mapContributionLines(assessment.getContributionLines()) : List.of()
        );
    }

    private ScreeningResult mapScreeningResult(Object screeningSnapshot) {
        if (Objects.isNull(screeningSnapshot)) {
            return null;
        }
        return objectMapper.convertValue(screeningSnapshot, ScreeningResult.class);
    }

    private List<CategoryResultDto> mapCategoryResults(List<CategoryResult> results) {
        if (Objects.isNull(results)) {
            return List.of();
        }
        return results.stream()
                .map(result -> new CategoryResultDto(result.getCategoryKey(), result.getCategoryScore(), result.getCategoryLevel()))
                .toList();
    }

    private List<ContributionLineDto> mapContributionLines(List<ContributionLine> lines) {
        if (Objects.isNull(lines)) {
            return List.of();
        }
        return lines.stream().map(this::mapContributionLine).toList();
    }

    private ContributionLineDto mapContributionLine(ContributionLine line) {
        return new ContributionLineDto(
                line.getCategoryKey(),
                line.getAttributeKey(),
                line.getRawValue(),
                line.getNormalizedValue(),
                Objects.isNull(line.getMatchedRule()) ? null : line.getMatchedRule().getId(),
                line.getPoints(),
                line.getWeightApplied(),
                line.getWeightedPoints(),
                line.getNote()
        );
    }
}
