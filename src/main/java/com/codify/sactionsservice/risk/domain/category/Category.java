package com.codify.sactionsservice.risk.domain.category;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.risk.domain.attribute.Attribute;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.risk.domain.threshold.Threshold;
import jakarta.persistence.*;
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
public class Category extends PO {

    private String key;

    private BigDecimal weight;

    private Integer ordering;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private RollUpMethod rollUpMethod;

    @Enumerated(EnumType.STRING)
    private Timing timing;

    @ManyToOne
    @JoinColumn(name = "risk_model_id")
    private RiskModel riskModel;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Threshold> thresholds;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Attribute> attributes;
}
