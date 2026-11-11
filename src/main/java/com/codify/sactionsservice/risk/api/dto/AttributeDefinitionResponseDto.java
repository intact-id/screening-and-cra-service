package com.codify.sactionsservice.risk.api.dto;

import com.codify.sactionsservice.risk.domain.attributedefinition.DataType;
import com.codify.sactionsservice.risk.domain.attributedefinition.SourceSystem;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;

public record AttributeDefinitionResponseDto(
        String key,
        String displayName,
        DataType dataType,
        SourceSystem sourceSystem,
        String description,
        String categoryKey,
        CustomerType customerType,
        boolean active
) {
}
