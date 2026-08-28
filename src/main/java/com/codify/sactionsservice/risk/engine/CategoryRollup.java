package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.RiskLevel;
import com.codify.sactionsservice.risk.domain.category.Timing;
import com.codify.sactionsservice.risk.domain.riskassessment.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CategoryRollup {

    private CategoryRollup() {
    }

    public static boolean inScope(CategorySnapshot category, Stage stage) {
        return category.timing() == Timing.ONBOARDING || stage == Stage.POST_ONBOARDING;
    }

    public static CategoryRollupResult rollup(CategorySnapshot category, AttributePayload payload) {
        List<ContributionLineResult> contributionLines = new ArrayList<>();
        List<WeightedPoints> weightedPoints = new ArrayList<>();

        for (AttributeSnapshot attribute : category.attributes()) {
            AttributeScoreResult result = AttributeScorer.score(attribute, payload);
            Object value = payload.get(attribute.attributeDefinitionKey());

            BigDecimal points = BigDecimal.valueOf(result.points());
            BigDecimal weightApplied = attribute.weight();
            BigDecimal weightedPointsValue = points.multiply(weightApplied);

            contributionLines.add(new ContributionLineResult(
                    category.key(),
                    attribute.attributeDefinitionKey(),
                    value,
                    value,
                    result.matchedRuleId(),
                    result.points(),
                    weightApplied,
                    weightedPointsValue,
                    result.note()
            ));

            weightedPoints.add(new WeightedPoints(points, weightApplied, weightedPointsValue));
        }

        BigDecimal categoryScore = switch (category.rollupMethod()) {
            case WEIGHTED_SUM -> weightedSum(weightedPoints);
            case WEIGHTED_AVG -> weightedAverage(weightedPoints);
            case AVERAGE -> average(weightedPoints);
            case MAX -> max(weightedPoints);
        };

        RiskLevel categoryLevel = bandFor(category.thresholds(), categoryScore);

        return new CategoryRollupResult(
                new CategoryScoreResult(category.key(), categoryScore, categoryLevel),
                contributionLines
        );
    }

    private static BigDecimal weightedSum(List<WeightedPoints> values) {
        return values.stream()
                .map(WeightedPoints::weightedPoints)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal weightedAverage(List<WeightedPoints> values) {
        var totalWeight = values.stream()
                .map(WeightedPoints::weight)
                .reduce(BigDecimal.ZERO,
                        BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return weightedSum(values).divide(totalWeight, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal average(List<WeightedPoints> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = values.stream().map(WeightedPoints::points).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal max(List<WeightedPoints> values) {
        return values.stream()
                .map(WeightedPoints::points)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    static RiskLevel bandFor(List<ThresholdBand> bands, BigDecimal score) {
        List<ThresholdBand> sorted = bands.stream()
                .sorted(Comparator.comparing(ThresholdBand::minScore))
                .toList();

        for (ThresholdBand band : sorted) {
            if (score.compareTo(band.minScore()) >= 0 && score.compareTo(band.maxScore()) <= 0) {
                return band.riskLevel();
            }
        }

        throw new IllegalStateException("Score " + score + " did not fall within any configured threshold band");
    }

    private record WeightedPoints(BigDecimal points, BigDecimal weight, BigDecimal weightedPoints) {
    }
}
