package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OverallCombiner {

    private OverallCombiner() {
    }

    public record CombinedResult(BigDecimal baseScore, RiskLevel overallLevel, boolean overrideApplied,
                                  List<OverrideEvaluationResult> overrideEvaluations) {
    }

    public static CombinedResult combine(RiskModelSnapshot model, List<CategoryScoreResult> categoryResults,
                                          AttributePayload payload) {
        BigDecimal baseScore = switch (model.combineMethod()) {
            case MAX -> maxCategoryScore(categoryResults);
            case WEIGHTED_SUM, HYBRID -> weightedCategorySum(model, categoryResults);
        };

        RiskLevel baseLevel = CategoryRollup.bandFor(model.overallThresholds(), baseScore);

        List<OverrideEvaluationResult> evaluations = new ArrayList<>();
        RiskLevel finalLevel = baseLevel;

        for (OverrideRuleSnapshot rule : model.overrideRules()) {
            boolean triggered = OverrideEvaluator.evaluate(rule.condition(), payload);
            evaluations.add(new OverrideEvaluationResult(rule.id(), triggered, rule.forceLevel()));

            if (triggered && rule.forceLevel().ordinal() > finalLevel.ordinal()) {
                finalLevel = rule.forceLevel();
            }
        }

        return new CombinedResult(baseScore, finalLevel, finalLevel != baseLevel, evaluations);
    }

    private static BigDecimal weightedCategorySum(RiskModelSnapshot model, List<CategoryScoreResult> categoryResults) {
        Map<String, BigDecimal> weightByCategoryKey = model.categories().stream()
                .collect(Collectors.toMap(CategorySnapshot::key, CategorySnapshot::weight));

        BigDecimal total = BigDecimal.ZERO;
        for (CategoryScoreResult result : categoryResults) {
            BigDecimal weight = weightByCategoryKey.get(result.categoryKey());
            total = total.add(result.categoryScore().multiply(weight));
        }
        return total;
    }

    private static BigDecimal maxCategoryScore(List<CategoryScoreResult> categoryResults) {
        return categoryResults.stream()
                .map(CategoryScoreResult::categoryScore)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}
