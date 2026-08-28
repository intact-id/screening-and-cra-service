package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.math.BigDecimal;

public record ThresholdBand(BigDecimal minScore, BigDecimal maxScore, RiskLevel riskLevel) {
}
