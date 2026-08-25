package com.codify.sactionsservice.risk.domain.categoryresult;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.risk.domain.RiskLevel;
import com.codify.sactionsservice.risk.domain.riskassessment.RiskAssessment;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResult extends PO {

    @ManyToOne
    @JoinColumn(name = "assessment_id")
    private RiskAssessment assessment;

    private String categoryKey;

    private BigDecimal categoryScore;

    @Enumerated(EnumType.STRING)
    private RiskLevel categoryLevel;
}
