package com.codify.sactionsservice.risk.config.mapper;

import com.codify.sactionsservice.risk.domain.attribute.Attribute;
import com.codify.sactionsservice.risk.domain.attribute.valuescorerule.ValueScoreRule;
import com.codify.sactionsservice.risk.domain.category.Category;
import com.codify.sactionsservice.risk.domain.overriderule.OverrideRule;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.risk.domain.threshold.Threshold;
import com.codify.sactionsservice.risk.engine.AttributeSnapshot;
import com.codify.sactionsservice.risk.engine.CategorySnapshot;
import com.codify.sactionsservice.risk.engine.OverrideRuleSnapshot;
import com.codify.sactionsservice.risk.engine.RiskModelSnapshot;
import com.codify.sactionsservice.risk.engine.ThresholdBand;
import com.codify.sactionsservice.risk.engine.ValueScoreRuleSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Freezes a loaded {@link RiskModel} entity graph into the plain,
 * JPA-free {@link RiskModelSnapshot} the scoring engine (risk.engine) consumes.
 * This is the one place JPA touches the engine boundary — call it inside the
 * transaction that loaded the entity graph.
 */
@Component
public class RiskModelSnapshotMapper {

    public RiskModelSnapshot toSnapshot(RiskModel riskModel) {
        return new RiskModelSnapshot(
                riskModel.getId(),
                riskModel.getVersion(),
                riskModel.getCombineMethod(),
                mapThresholdBands(riskModel.getThresholds()),
                mapOverrideRules(riskModel.getOverrideRules()),
                mapCategories(riskModel.getCategories())
        );
    }

    private List<CategorySnapshot> mapCategories(List<Category> categories) {
        if (Objects.isNull(categories)) {
            return List.of();
        }
        return categories.stream().map(this::mapCategory).toList();
    }

    private CategorySnapshot mapCategory(Category category) {
        return new CategorySnapshot(
                category.getKey(),
                category.getWeight(),
                category.getRollUpMethod(),
                category.getTiming(),
                mapThresholdBands(category.getThresholds()),
                mapAttributes(category.getAttributes())
        );
    }

    private List<AttributeSnapshot> mapAttributes(List<Attribute> attributes) {
        if (Objects.isNull(attributes)) {
            return List.of();
        }
        return attributes.stream().map(this::mapAttribute).toList();
    }

    private AttributeSnapshot mapAttribute(Attribute attribute) {
        return new AttributeSnapshot(
                attribute.getAttributeDefinitionKey(),
                attribute.getWeight(),
                Boolean.TRUE.equals(attribute.getIsRequired()),
                attribute.getMissingBehavior(),
                attribute.getDefaultScore(),
                mapRules(attribute.getRules())
        );
    }

    private List<ValueScoreRuleSnapshot> mapRules(List<ValueScoreRule> rules) {
        if (Objects.isNull(rules)) {
            return List.of();
        }
        return rules.stream().map(this::mapRule).toList();
    }

    private ValueScoreRuleSnapshot mapRule(ValueScoreRule rule) {
        return new ValueScoreRuleSnapshot(rule.getId(), rule.getMatchType(), rule.getMatchValue(), rule.getPriority(), rule.getScore());
    }

    private List<ThresholdBand> mapThresholdBands(List<Threshold> thresholds) {
        if (Objects.isNull(thresholds)) {
            return List.of();
        }
        return thresholds.stream().map(this::mapThresholdBand).toList();
    }

    private ThresholdBand mapThresholdBand(Threshold threshold) {
        return new ThresholdBand(threshold.getMinScore(), threshold.getMaxScore(), threshold.getRiskLevel());
    }

    private List<OverrideRuleSnapshot> mapOverrideRules(List<OverrideRule> overrideRules) {
        if (Objects.isNull(overrideRules)) {
            return List.of();
        }
        return overrideRules.stream().map(this::mapOverrideRule).toList();
    }

    private OverrideRuleSnapshot mapOverrideRule(OverrideRule overrideRule) {
        return new OverrideRuleSnapshot(overrideRule.getId(), overrideRule.getCondition(), overrideRule.getForceLevel(), overrideRule.getReason());
    }
}
