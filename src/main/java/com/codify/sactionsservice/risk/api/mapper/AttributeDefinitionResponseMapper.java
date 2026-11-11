package com.codify.sactionsservice.risk.api.mapper;

import com.codify.sactionsservice.risk.api.dto.AttributeDefinitionResponseDto;
import com.codify.sactionsservice.risk.domain.attributedefinition.AttributeDefinition;
import org.springframework.stereotype.Component;

@Component
public class AttributeDefinitionResponseMapper {

    public AttributeDefinitionResponseDto toDto(AttributeDefinition definition) {
        return new AttributeDefinitionResponseDto(
                definition.getKey(),
                definition.getDisplayName(),
                definition.getDataType(),
                definition.getSourceSystem(),
                definition.getDescription(),
                definition.getCategoryKey(),
                definition.getCustomerType(),
                Boolean.TRUE.equals(definition.getIsActive())
        );
    }
}
