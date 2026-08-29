package com.codify.sactionsservice.risk.domain.riskassessment;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.common.converter.PgJsonbToMapConverter;
import com.codify.sactionsservice.risk.domain.RiskLevel;
import com.codify.sactionsservice.risk.domain.categoryresult.CategoryResult;
import com.codify.sactionsservice.risk.domain.contributionline.ContributionLine;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.tenant.domain.Tenant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment extends PO {
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    @ManyToOne
    @JoinColumn(name = "risk_model_id")
    private RiskModel riskModel;

    private Integer modelVersion;

    @Enumerated(EnumType.STRING)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    private AssessmentStatus status;

    private BigDecimal overallScore;

    @Enumerated(EnumType.STRING)
    private RiskLevel overallLevel;

    private Boolean overrideApplied;

    @Enumerated(EnumType.STRING)
    private TriggeredBy triggeredBy;

    @Convert(converter = PgJsonbToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> inputSnapshot;

    @Convert(converter = PgJsonbToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fx_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> fxSnapshot;

    @Convert(converter = PgJsonbToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "screening_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> screeningSnapshot;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL)
    private List<CategoryResult> categoryResults;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL)
    private List<ContributionLine> contributionLines;

}
