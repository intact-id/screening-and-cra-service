package com.codify.sactionsservice.risk.api.dto;

import com.codify.sactionsservice.risk.domain.riskmodel.CombineMethod;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.domain.riskmodel.LifeCycleStatus;

import java.time.LocalDateTime;

public record RiskModelResponseDto(
        String id,
        Integer version,
        String description,
        CustomerType customerType,
        LifeCycleStatus lifeCycleStatus,
        CombineMethod combineMethod,
        String tenantId,
        LocalDateTime effectiveFrom
) {
}
