package com.codify.sactionsservice.risk.domain.contributionline;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.common.converter.PgJsonbToObjectConverter;
import com.codify.sactionsservice.risk.domain.attribute.valuescorerule.ValueScoreRule;
import com.codify.sactionsservice.risk.domain.riskassessment.RiskAssessment;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionLine extends PO {

    @ManyToOne
    @JoinColumn(name = "assessment_id")
    private RiskAssessment assessment;

    private String categoryKey;

    private String attributeKey;

    @Convert(converter = PgJsonbToObjectConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_value", columnDefinition = "jsonb")
    private Object rawValue;

    @Convert(converter = PgJsonbToObjectConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "normalized_value", columnDefinition = "jsonb")
    private Object normalizedValue;

    @ManyToOne
    @JoinColumn(name = "matched_rule_id")
    private ValueScoreRule matchedRule;

    private Integer points;

    private BigDecimal weightApplied;

    private BigDecimal weightedPoints;

    private String note;
}
