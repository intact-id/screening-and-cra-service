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
import com.codify.sactionsservice.risk.domain.riskmodel.LifeCycleStatus;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.risk.domain.threshold.OwnerType;
import com.codify.sactionsservice.risk.domain.threshold.Threshold;
import com.codify.sactionsservice.tenant.domain.Tenant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Builds the persisted entity graph from a validated {@link RiskModelConfig}.
 * Only ever called after {@code RiskModelConfigValidator} has passed — this
 * class trusts the shape it's given and does no validation of its own.
 */
@Component
public class RiskModelConfigMapper {

    public RiskModel toEntity(RiskModelConfig config, Tenant tenant, Integer version) {
        RiskModel riskModel = RiskModel.builder()
                .tenant(tenant)
                .version(version)
                .description(config.description())
                .customerType(config.customerType())
                .combineMethod(config.combineMethod())
                .lifeCycleStatus(LifeCycleStatus.DRAFT)
                .build();

        riskModel.setCategories(mapCategories(config.categories(), riskModel));
        riskModel.setThresholds(mapThresholds(config.overallThresholds(), null, riskModel, OwnerType.MODEL));
        riskModel.setOverrideRules(mapOverrideRules(config.overrideRules(), riskModel));

        return riskModel;
    }

    private List<Category> mapCategories(List<CategoryConfig> configs, RiskModel riskModel) {
        if (Objects.isNull(configs)) {
            return List.of();
        }
        return configs.stream().map(config -> mapCategory(config, riskModel)).toList();
    }

    private Category mapCategory(CategoryConfig config, RiskModel riskModel) {
        Category category = Category.builder()
                .riskModel(riskModel)
                .key(config.key())
                .displayName(config.displayName())
                .weight(config.weight())
                .rollUpMethod(config.rollupMethod())
                .timing(config.timing())
                .ordering(config.ordering())
                .build();

        category.setThresholds(mapThresholds(config.thresholds(), category, null, OwnerType.CATEGORY));
        category.setAttributes(mapAttributes(config.attributes(), category));

        return category;
    }

    private List<Attribute> mapAttributes(List<AttributeConfig> configs, Category category) {
        if (Objects.isNull(configs)) {
            return List.of();
        }
        return configs.stream().map(config -> mapAttribute(config, category)).toList();
    }

    private Attribute mapAttribute(AttributeConfig config, Category category) {
        Attribute attribute = Attribute.builder()
                .category(category)
                .attributeDefinitionKey(config.attributeDefinitionKey())
                .weight(config.weight())
                .isRequired(config.required())
                .missingBehavior(config.missingBehavior())
                .defaultScore(config.defaultScore())
                .build();

        attribute.setRules(mapRules(config.rules(), attribute));

        return attribute;
    }

    private List<ValueScoreRule> mapRules(List<ValueScoreRuleConfig> configs, Attribute attribute) {
        if (Objects.isNull(configs)) {
            return List.of();
        }
        return configs.stream().map(config -> mapRule(config, attribute)).toList();
    }

    private ValueScoreRule mapRule(ValueScoreRuleConfig config, Attribute attribute) {
        return ValueScoreRule.builder()
                .attribute(attribute)
                .matchType(config.matchType())
                .matchValue(config.matchValue())
                .priority(config.priority())
                .score(config.score())
                .build();
    }

    private List<Threshold> mapThresholds(List<ThresholdConfig> configs, Category category, RiskModel riskModel, OwnerType ownerType) {
        if (Objects.isNull(configs)) {
            return List.of();
        }
        return configs.stream().map(config -> mapThreshold(config, category, riskModel, ownerType)).toList();
    }

    private Threshold mapThreshold(ThresholdConfig config, Category category, RiskModel riskModel, OwnerType ownerType) {
        return Threshold.builder()
                .category(category)
                .riskModel(riskModel)
                .minScore(config.minScore())
                .maxScore(config.maxScore())
                .riskLevel(config.riskLevel())
                .ownerType(ownerType)
                .build();
    }

    private List<OverrideRule> mapOverrideRules(List<OverrideRuleConfig> configs, RiskModel riskModel) {
        if (Objects.isNull(configs)) {
            return List.of();
        }
        return configs.stream().map(config -> mapOverrideRule(config, riskModel)).toList();
    }

    private OverrideRule mapOverrideRule(OverrideRuleConfig config, RiskModel riskModel) {
        return OverrideRule.builder()
                .riskModel(riskModel)
                .forceLevel(config.forceLevel())
                .condition(config.condition())
                .reason(config.reason())
                .build();
    }
}
