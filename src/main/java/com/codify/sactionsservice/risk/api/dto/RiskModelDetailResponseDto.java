package com.codify.sactionsservice.risk.api.dto;

import com.codify.sactionsservice.risk.config.dto.RiskModelConfig;

public record RiskModelDetailResponseDto(
        RiskModelResponseDto model,
        RiskModelConfig config
) {
}
