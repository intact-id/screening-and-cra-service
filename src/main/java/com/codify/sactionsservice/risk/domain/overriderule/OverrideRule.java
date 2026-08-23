package com.codify.sactionsservice.risk.domain.overriderule;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.common.converter.PgJsonbToMapConverter;
import com.codify.sactionsservice.risk.domain.RiskLevel;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OverrideRule extends PO {

    @Enumerated(EnumType.STRING)
    private RiskLevel forceLevel;

    @Convert(converter = PgJsonbToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition", columnDefinition = "jsonb")
    private Map<String, Object> condition;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "risk_model_id")
    private RiskModel riskModel;

}
