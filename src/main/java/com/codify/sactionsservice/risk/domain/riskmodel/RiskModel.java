package com.codify.sactionsservice.risk.domain.riskmodel;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.risk.domain.category.Category;
import com.codify.sactionsservice.risk.domain.overriderule.OverrideRule;
import com.codify.sactionsservice.risk.domain.threshold.Threshold;
import com.codify.sactionsservice.tenant.domain.Tenant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RiskModel extends PO {

    private Integer version;

    private String description;

    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    @Enumerated(EnumType.STRING)
    private LifeCycleStatus lifeCycleStatus;

    @Enumerated(EnumType.STRING)
    private CombineMethod combineMethod;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @OneToMany(mappedBy = "riskModel", cascade = CascadeType.ALL)
    private List<OverrideRule> overrideRules;

    @OneToMany(mappedBy = "riskModel", cascade = CascadeType.ALL)
    private List<Threshold> thresholds;

    @OneToMany(mappedBy = "riskModel", cascade = CascadeType.ALL)
    private List<Category> categories;

}
