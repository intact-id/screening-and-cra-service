package com.codify.sactionsservice.risk.domain.attribute;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.risk.domain.attribute.valuescorerule.ValueScoreRule;
import com.codify.sactionsservice.risk.domain.category.Category;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute extends PO {

    private String attributeDefinitionKey;

    private BigDecimal weight;

    private Boolean isRequired;

    private Integer defaultScore;

    @Enumerated(EnumType.STRING)
    private MissingBehavior missingBehavior;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL)
    private List<ValueScoreRule> rules;
}
