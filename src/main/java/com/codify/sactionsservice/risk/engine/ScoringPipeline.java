package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.riskassessment.Stage;

import java.util.ArrayList;
import java.util.List;

public final class ScoringPipeline {

    private ScoringPipeline() {
    }

    public static ScoringResult run(RiskModelSnapshot model, AttributePayload payload, Stage stage) {
        List<CategoryScoreResult> categoryResults = new ArrayList<>();
        List<ContributionLineResult> contributionLines = new ArrayList<>();

        for (CategorySnapshot category : model.categories()) {
            if (!CategoryRollup.inScope(category, stage)) {
                continue;
            }

            CategoryRollupResult rollup = CategoryRollup.rollup(category, payload);
            categoryResults.add(rollup.categoryScore());
            contributionLines.addAll(rollup.contributionLines());
        }

        OverallCombiner.CombinedResult combined = OverallCombiner.combine(model, categoryResults, payload);

        return new ScoringResult(
                combined.baseScore(),
                combined.overallLevel(),
                combined.overrideApplied(),
                categoryResults,
                contributionLines,
                combined.overrideEvaluations()
        );
    }
}
