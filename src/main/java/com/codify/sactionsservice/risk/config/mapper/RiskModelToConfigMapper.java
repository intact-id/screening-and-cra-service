package com.codify.sactionsservice.risk.config.mapper;

import com.codify.sactionsservice.risk.config.dto.AttributeConfig;
import com.codify.sactionsservice.risk.config.dto.CategoryConfig;
import com.codify.sactionsservice.risk.config.dto.OverrideRuleConfig;
import com.codify.sactionsservice.risk.config.dto.RiskModelConfig;
import com.codify.sactionsservice.risk.config.dto.ThresholdConfig;
import com.codify.sactionsservice.risk.config.dto.ValueScoreRuleConfig;
import com.codify.sactionsservice.risk.domain.attribute.Attribute;
import com.codify.sactionsservice.risk.domain.attribute.valuescorerule.ValueScoreRule;
import com.codify.sactionsservice.risk.domain.category.Category;
import com.codify.sactionsservice.risk.domain.overriderule.OverrideRule;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.risk.domain.threshold.Threshold;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Reverses {@link RiskModelConfigMapper#toEntity}. Used by
 * {@code RiskModelService.activate()} to re-run
 * {@code RiskModelConfigValidator.validateForActivation} against an
 * already-persisted {@link RiskModel} — the sanctions-override guardrail is a
 * warning at draft time but a hard block at activation, so it must be re-checked.
 */
@Component
public class RiskModelToConfigMapper {

    public RiskModelConfig toConfig(RiskModel riskModel) {
        return new RiskModelConfig(
                riskModel.getCustomerType(),
                riskModel.getCombineMethod(),
                riskModel.getDescription(),
                mapThresholdConfigs(riskModel.getThresholds()),
                mapOverrideRuleConfigs(riskModel.getOverrideRules()),
                mapCategoryConfigs(riskModel.getCategories())
        );
    }

    private List<CategoryConfig> mapCategoryConfigs(List<Category> categories) {
        if (Objects.isNull(categories)) {
            return List.of();
        }
        return categories.stream().map(this::mapCategoryConfig).toList();
    }

    private CategoryConfig mapCategoryConfig(Category category) {
        return new CategoryConfig(
                category.getKey(),
                category.getDisplayName(),
                category.getWeight(),
                category.getRollUpMethod(),
                category.getTiming(),
                category.getOrdering(),
                mapThresholdConfigs(category.getThresholds()),
                mapAttributeConfigs(category.getAttributes())
        );
    }

    private List<AttributeConfig> mapAttributeConfigs(List<Attribute> attributes) {
        if (Objects.isNull(attributes)) {
            return List.of();
        }
        return attributes.stream().map(this::mapAttributeConfig).toList();
    }

    private AttributeConfig mapAttributeConfig(Attribute attribute) {
        return new AttributeConfig(
                attribute.getAttributeDefinitionKey(),
                attribute.getWeight(),
                attribute.getIsRequired(),
                attribute.getMissingBehavior(),
                attribute.getDefaultScore(),
                mapRuleConfigs(attribute.getRules())
        );
    }

    private List<ValueScoreRuleConfig> mapRuleConfigs(List<ValueScoreRule> rules) {
        if (Objects.isNull(rules)) {
            return List.of();
        }
        return rules.stream().map(this::mapRuleConfig).toList();
    }

    private ValueScoreRuleConfig mapRuleConfig(ValueScoreRule rule) {
        return new ValueScoreRuleConfig(rule.getMatchType(), rule.getMatchValue(), rule.getScore(), rule.getPriority());
    }

    private List<ThresholdConfig> mapThresholdConfigs(List<Threshold> thresholds) {
        if (Objects.isNull(thresholds)) {
            return List.of();
        }
        return thresholds.stream().map(this::mapThresholdConfig).toList();
    }

    private ThresholdConfig mapThresholdConfig(Threshold threshold) {
        return new ThresholdConfig(threshold.getRiskLevel(), threshold.getMinScore(), threshold.getMaxScore());
    }

    private List<OverrideRuleConfig> mapOverrideRuleConfigs(List<OverrideRule> overrideRules) {
        if (Objects.isNull(overrideRules)) {
            return List.of();
        }
        return overrideRules.stream().map(this::mapOverrideRuleConfig).toList();
    }

    private OverrideRuleConfig mapOverrideRuleConfig(OverrideRule overrideRule) {
        return new OverrideRuleConfig(overrideRule.getCondition(), overrideRule.getForceLevel(), overrideRule.getReason());
    }
}
