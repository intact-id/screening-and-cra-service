package com.codify.sactionsservice.risk.api.mapper;

import com.codify.sactionsservice.risk.api.dto.RiskModelResponseDto;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import org.springframework.stereotype.Component;

@Component
public class RiskModelResponseMapper {

    public RiskModelResponseDto toDto(RiskModel riskModel) {
        return new RiskModelResponseDto(
                riskModel.getId(),
                riskModel.getVersion(),
                riskModel.getDescription(),
                riskModel.getCustomerType(),
                riskModel.getLifeCycleStatus(),
                riskModel.getCombineMethod(),
                riskModel.getTenant().getId(),
                riskModel.getEffectiveFrom()
        );
    }
}
