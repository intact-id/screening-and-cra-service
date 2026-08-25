package com.codify.sactionsservice.risk.domain.attributedefinition;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefinition extends PO {

    @Column(unique = true)
    private String key;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @Enumerated(EnumType.STRING)
    private SourceSystem sourceSystem;

    private String description;

    private String categoryKey;

    @Enumerated(EnumType.STRING)
    private CustomerType customerType;
}
