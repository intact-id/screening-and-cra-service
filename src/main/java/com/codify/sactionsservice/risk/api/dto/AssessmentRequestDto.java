package com.codify.sactionsservice.risk.api.dto;

import com.codify.sactionsservice.risk.domain.riskassessment.Stage;
import com.codify.sactionsservice.risk.domain.riskassessment.TriggeredBy;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.screening.api.dto.BusinessMatchDto;
import com.codify.sactionsservice.screening.api.dto.PersonMatchDto;

import java.util.Map;

public record AssessmentRequestDto(
        String tenantId,
        String customerId,
        CustomerType customerType,
        Stage stage,
        Map<String, Object> attributePayload,
        TriggeredBy triggeredBy,
        PersonMatchDto personIdentity,
        BusinessMatchDto businessIdentity
) {
}
