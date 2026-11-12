package com.codify.sactionsservice.risk.service;

import com.codify.sactionsservice.common.exception.ApplicationException;
import com.codify.sactionsservice.risk.api.dto.AttributeDefinitionResponseDto;
import com.codify.sactionsservice.risk.api.mapper.AttributeDefinitionResponseMapper;
import com.codify.sactionsservice.risk.domain.attributedefinition.AttributeDefinition;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.repository.AttributeDefinitionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeDefinitionService {

    private final AttributeDefinitionRepo attributeDefinitionRepo;
    private final AttributeDefinitionResponseMapper responseMapper;

    @Transactional(readOnly = true)
    public List<AttributeDefinitionResponseDto> list(CustomerType customerType, String categoryKey) {
        return attributeDefinitionRepo.findAllByIsActiveTrueOrderByCategoryKeyAscKeyAsc().stream()
                .filter(definition -> appliesTo(definition, customerType))
                .filter(definition -> categoryKey == null || categoryKey.equals(definition.getCategoryKey()))
                .map(responseMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttributeDefinitionResponseDto getByKey(String key) {
        return attributeDefinitionRepo.findByKey(key)
                .map(responseMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("AttributeDefinition not found: key={}", key);
                    return new ApplicationException.ResourceNotFoundException("Attribute definition not found");
                });
    }

    private boolean appliesTo(AttributeDefinition definition, CustomerType customerType) {
        if (customerType == null || customerType == CustomerType.BOTH) {
            return true;
        }
        return definition.getCustomerType() == customerType || definition.getCustomerType() == CustomerType.BOTH;
    }
}
