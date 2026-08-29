package com.codify.sactionsservice.risk.config.mapper;

import com.codify.sactionsservice.risk.domain.categoryresult.CategoryResult;
import com.codify.sactionsservice.risk.domain.contributionline.ContributionLine;
import com.codify.sactionsservice.risk.domain.riskassessment.AssessmentStatus;
import com.codify.sactionsservice.risk.domain.riskassessment.RiskAssessment;
import com.codify.sactionsservice.risk.domain.riskassessment.Stage;
import com.codify.sactionsservice.risk.domain.riskassessment.TriggeredBy;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.risk.engine.CategoryScoreResult;
import com.codify.sactionsservice.risk.engine.ContributionLineResult;
import com.codify.sactionsservice.risk.engine.ScoringResult;
import com.codify.sactionsservice.risk.repository.ValueScoreRuleRepo;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import com.codify.sactionsservice.tenant.domain.Tenant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ScoringResultMapper {

    private final ValueScoreRuleRepo valueScoreRuleRepo;
    private final ObjectMapper objectMapper;

    public RiskAssessment toEntity(Tenant tenant, RiskModel riskModel, String customerId, CustomerType customerType,
                                    Stage stage, TriggeredBy triggeredBy, Map<String, Object> inputSnapshot,
                                    ScoringResult result, ScreeningResult screeningResult) {
        RiskAssessment assessment = RiskAssessment.builder()
                .tenant(tenant)
                .customerId(customerId)
                .customerType(customerType)
                .riskModel(riskModel)
                .modelVersion(riskModel.getVersion())
                .stage(stage)
                .status(result.incomplete() ? AssessmentStatus.INCOMPLETE : AssessmentStatus.COMPLETE)
                .overallScore(result.overallScore())
                .overallLevel(result.overallLevel())
                .overrideApplied(result.overrideApplied())
                .triggeredBy(triggeredBy)
                .inputSnapshot(inputSnapshot)
                .fxSnapshot(Map.of())
                .screeningSnapshot(objectMapper.convertValue(screeningResult, new TypeReference<Map<String, Object>>() {
                }))
                .build();

        assessment.setCategoryResults(mapCategoryResults(result.categoryResults(), assessment));
        assessment.setContributionLines(mapContributionLines(result.contributionLines(), assessment));

        return assessment;
    }

    private List<CategoryResult> mapCategoryResults(List<CategoryScoreResult> results, RiskAssessment assessment) {
        if (Objects.isNull(results)) {
            return List.of();
        }
        return results.stream().map(result -> mapCategoryResult(result, assessment)).toList();
    }

    private CategoryResult mapCategoryResult(CategoryScoreResult result, RiskAssessment assessment) {
        return CategoryResult.builder()
                .assessment(assessment)
                .categoryKey(result.categoryKey())
                .categoryScore(result.categoryScore())
                .categoryLevel(result.categoryLevel())
                .build();
    }

    private List<ContributionLine> mapContributionLines(List<ContributionLineResult> lines, RiskAssessment assessment) {
        if (Objects.isNull(lines)) {
            return List.of();
        }
        return lines.stream().map(line -> mapContributionLine(line, assessment)).toList();
    }

    private ContributionLine mapContributionLine(ContributionLineResult line, RiskAssessment assessment) {
        return ContributionLine.builder()
                .assessment(assessment)
                .categoryKey(line.categoryKey())
                .attributeKey(line.attributeKey())
                .rawValue(line.rawValue())
                .normalizedValue(line.normalizedValue())
                .matchedRule(Objects.isNull(line.matchedRuleId()) ? null : valueScoreRuleRepo.getReferenceById(line.matchedRuleId()))
                .points(line.points())
                .weightApplied(line.weightApplied())
                .weightedPoints(line.weightedPoints())
                .note(line.note())
                .build();
    }
}
