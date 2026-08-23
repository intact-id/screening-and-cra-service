package com.codify.sactionsservice.risk.domain.threshold;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.risk.domain.RiskLevel;
import com.codify.sactionsservice.risk.domain.category.Category;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import jakarta.persistence.*;
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
@EntityListeners(ThresholdOwnerValidator.class)
public class Threshold extends PO {

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "risk_model_id")
    private RiskModel riskModel;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    @Enumerated(EnumType.STRING)
    private OwnerType ownerType;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
}
