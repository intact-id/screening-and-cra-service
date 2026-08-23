package com.codify.sactionsservice.tenant.domain;


import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.common.converter.PgJsonbToMapConverter;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.tenant.api.dto.TenantResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends PO {

    @Column(name = "tenant_identifier")
    private String tenantIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type")
    private TenantType tenantType;

    @Column(name = "tenant_name")
    private String tenantName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Convert(converter = PgJsonbToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_details", columnDefinition = "jsonb")
    private Map<String, Object> extraDetails;

    @OneToMany(mappedBy = "tenant")
    private List<RiskModel> riskModels;

    public TenantResponseDto toDto() {
        return new TenantResponseDto(
                this.tenantIdentifier,
                this.tenantType,
                this.tenantName,
                this.description,
                this.extraDetails,
                this.getIsActive(),   // from PO
                this.getId()          // from PO
        );
    }
}

