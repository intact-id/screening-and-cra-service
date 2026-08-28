package com.codify.sactionsservice.risk.config.validator;

import com.codify.sactionsservice.risk.config.dto.AttributeConfig;
import com.codify.sactionsservice.risk.config.dto.CategoryConfig;
import com.codify.sactionsservice.risk.config.dto.OverrideRuleConfig;
import com.codify.sactionsservice.risk.config.dto.RiskModelConfig;
import com.codify.sactionsservice.risk.config.dto.ThresholdConfig;
import com.codify.sactionsservice.risk.config.dto.ValueScoreRuleConfig;
import com.codify.sactionsservice.risk.domain.RiskLevel;
import com.codify.sactionsservice.risk.domain.attribute.MissingBehavior;
import com.codify.sactionsservice.risk.domain.attributedefinition.AttributeDefinition;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.repository.AttributeDefinitionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RiskModelConfigValidator {

    private static final String SANCTIONS_KEY = "sanctions";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final AttributeDefinitionRepo attributeDefinitionRepo;

    public ValidationResult validateForDraft(RiskModelConfig config) {
        return validate(config, false);
    }

    public ValidationResult validateForActivation(RiskModelConfig config) {
        return validate(config, true);
    }

    private ValidationResult validate(RiskModelConfig config, boolean forActivation) {
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();
        Set<String> configuredAttributeKeys = new HashSet<>();

        List<CategoryConfig> categories = config.categories() == null ? List.of() : config.categories();
        Set<String> categoryKeys = new HashSet<>();

        for (int i = 0; i < categories.size(); i++) {
            CategoryConfig category = categories.get(i);
            String categoryPath = "categories[" + i + "]";

            if (category.key() == null || !categoryKeys.add(category.key())) {
                errors.add(new ValidationError(categoryPath + ".key", "Category key is missing or duplicated: " + category.key()));
            }

            if (category.weight() == null || category.weight().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(new ValidationError(categoryPath + ".weight", "Category weight must be present and greater than zero"));
            }

            validateThresholdBands(category.thresholds(), categoryPath + ".thresholds", errors);
            validateAttributes(category, config, categoryPath, configuredAttributeKeys, errors);
        }

        validateThresholdBands(config.overallThresholds(), "overallThresholds", errors);

        boolean hasSanctionsGuardrail = validateOverrideRules(config.overrideRules(), configuredAttributeKeys, errors);
        if (!hasSanctionsGuardrail) {
            String message = "Model has no OverrideRule forcing HIGH on a confirmed sanctions match "
                    + "({\"attribute\":\"sanctions\",\"op\":\"eq\",\"value\":\"CONFIRMED_MATCH\"})";
            if (forActivation) {
                errors.add(new ValidationError("overrideRules", message));
            } else {
                warnings.add(new ValidationWarning("overrideRules", message));
            }
        }

        return new ValidationResult(errors, warnings);
    }

    private void validateAttributes(CategoryConfig category, RiskModelConfig config, String categoryPath,
                                     Set<String> configuredAttributeKeys, List<ValidationError> errors) {
        List<AttributeConfig> attributes = category.attributes() == null ? List.of() : category.attributes();
        Set<String> attributeKeysInCategory = new HashSet<>();

        for (int j = 0; j < attributes.size(); j++) {
            AttributeConfig attribute = attributes.get(j);
            String attributePath = categoryPath + ".attributes[" + j + "]";

            if (Objects.isNull(attribute.attributeDefinitionKey() )) {
                errors.add(new ValidationError(attributePath + ".attributeDefinitionKey", "attributeDefinitionKey is required"));
            } else {
                if (!attributeKeysInCategory.add(attribute.attributeDefinitionKey())) {
                    errors.add(new ValidationError(attributePath + ".attributeDefinitionKey",
                            "Duplicate attributeDefinitionKey in category: " + attribute.attributeDefinitionKey()));
                }
                configuredAttributeKeys.add(attribute.attributeDefinitionKey());
                validateAttributeDefinitionReference(attribute, category, config, attributePath, errors);
            }

            if (Boolean.TRUE.equals(attribute.required()) && attribute.missingBehavior() == MissingBehavior.DEFAULT_SCORE) {
                errors.add(new ValidationError(attributePath + ".missingBehavior",
                        "A required attribute cannot use DEFAULT_SCORE as its missing behavior"));
            }

            List<ValueScoreRuleConfig> rules = attribute.rules() == null ? List.of() : attribute.rules();
            for (int k = 0; k < rules.size(); k++) {
                Integer score = rules.get(k).score();
                if (score == null || score < 0 || score > 100) {
                    errors.add(new ValidationError(attributePath + ".rules[" + k + "].score", "Score must be between 0 and 100"));
                }
            }
        }
    }

    private void validateAttributeDefinitionReference(AttributeConfig attribute, CategoryConfig category,
                                                        RiskModelConfig config, String path, List<ValidationError> errors) {
        attributeDefinitionRepo.findByKey(attribute.attributeDefinitionKey())
                .ifPresentOrElse(
                def -> {
                    if (!Objects.equals(def.getCategoryKey(), category.key())) {
                        errors.add(new ValidationError(path + ".attributeDefinitionKey", "'" + attribute.attributeDefinitionKey()
                                + "' belongs to category '" + def.getCategoryKey() + "', not '" + category.key() + "'"));
                    }

                    if (def.getCustomerType() != CustomerType.BOTH && def.getCustomerType() != config.customerType()) {
                        errors.add(new ValidationError(path + ".attributeDefinitionKey",
                                "'" + attribute.attributeDefinitionKey() + "' is scoped to " + def.getCustomerType()
                                        + ", not usable on a " + config.customerType() + " model"));
                    }
                },
                () -> errors.add(new ValidationError(path + ".attributeDefinitionKey",
                        "No AttributeDefinition found for key: " + attribute.attributeDefinitionKey()))
        );
    }

    private void validateThresholdBands(List<ThresholdConfig> thresholds, String path, List<ValidationError> errors) {
        List<ThresholdConfig> bands = thresholds == null ? List.of() : thresholds;
        if (bands.isEmpty()) {
            errors.add(new ValidationError(path, "At least one threshold band is required"));
            return;
        }

        List<ThresholdConfig> sorted = bands.stream()
                .filter(band -> band.minScore() != null && band.maxScore() != null)
                .sorted(Comparator.comparing(ThresholdConfig::minScore))
                .toList();

        if (sorted.size() != bands.size()) {
            errors.add(new ValidationError(path, "Every threshold band requires both minScore and maxScore"));
            return;
        }

        if (sorted.getFirst().minScore().compareTo(BigDecimal.ZERO) != 0) {
            errors.add(new ValidationError(path, "Threshold bands must start at 0"));
        }

        BigDecimal expectedNextMin = null;
        for (int i = 0; i < sorted.size(); i++) {
            ThresholdConfig band = sorted.get(i);
            String bandPath = path + "[" + i + "]";

            if (band.minScore().compareTo(band.maxScore()) >= 0) {
                errors.add(new ValidationError(bandPath, "minScore must be less than maxScore"));
            } else if (expectedNextMin != null && band.minScore().compareTo(expectedNextMin) != 0) {
                errors.add(new ValidationError(bandPath,
                        "Gap or overlap between threshold bands: expected minScore " + expectedNextMin + " but got " + band.minScore()));
            }
            expectedNextMin = band.maxScore();
        }

        if (expectedNextMin != null && expectedNextMin.compareTo(HUNDRED) != 0) {
            errors.add(new ValidationError(path, "Threshold bands must end at 100"));
        }
    }

    private boolean validateOverrideRules(List<OverrideRuleConfig> overrideRules, Set<String> configuredAttributeKeys,
                                           List<ValidationError> errors) {
        List<OverrideRuleConfig> rules = overrideRules == null ? List.of() : overrideRules;
        boolean hasSanctionsGuardrail = false;

        for (int i = 0; i < rules.size(); i++) {
            OverrideRuleConfig rule = rules.get(i);
            String rulePath = "overrideRules[" + i + "]";

            if (rule.forceLevel() == RiskLevel.LOW) {
                errors.add(new ValidationError(rulePath + ".forceLevel", "An override rule can never force LOW"));
            }

            if (rule.condition() != null) {
                validateConditionAttributeReferences(rule.condition(), configuredAttributeKeys, rulePath + ".condition", errors);
                if (rule.forceLevel() == RiskLevel.HIGH && referencesConfirmedSanctionsMatch(rule.condition())) {
                    hasSanctionsGuardrail = true;
                }
            }
        }

        return hasSanctionsGuardrail;
    }

    @SuppressWarnings("unchecked")
    private void    validateConditionAttributeReferences(Map<String, Object> condition, Set<String> configuredAttributeKeys,
                                                        String path, List<ValidationError> errors) {
        Object attribute = condition.get("attribute");
        if (attribute instanceof String key) {
            if (!SANCTIONS_KEY.equals(key) && !configuredAttributeKeys.contains(key)) {
                errors.add(new ValidationError(path + ".attribute", "Condition references unknown attribute key: " + key));
            }
            return;
        }

        for (String composite : List.of("all", "any")) {
            if (condition.get(composite) instanceof List<?> children) {
                for (int i = 0; i < children.size(); i++) {
                    if (children.get(i) instanceof Map<?, ?> child) {
                        validateConditionAttributeReferences((Map<String, Object>) child, configuredAttributeKeys,
                                path + "." + composite + "[" + i + "]", errors);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean referencesConfirmedSanctionsMatch(Map<String, Object> condition) {
        Object attribute = condition.get("attribute");
        if (attribute != null) {
            return SANCTIONS_KEY.equals(attribute)
                    && "eq".equals(condition.get("op"))
                    && "CONFIRMED_MATCH".equals(condition.get("value"));
        }

        for (String composite : List.of("all", "any")) {
            if (condition.get(composite) instanceof List<?> children) {
                for (Object child : children) {
                    if (child instanceof Map<?, ?> childMap && referencesConfirmedSanctionsMatch((Map<String, Object>) childMap)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
