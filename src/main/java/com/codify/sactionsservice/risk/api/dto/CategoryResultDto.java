package com.codify.sactionsservice.risk.api.dto;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.math.BigDecimal;

public record CategoryResultDto(String categoryKey, BigDecimal categoryScore, RiskLevel categoryLevel) {
}
