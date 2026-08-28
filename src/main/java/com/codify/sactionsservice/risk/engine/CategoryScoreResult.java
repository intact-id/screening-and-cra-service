package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.RiskLevel;

import java.math.BigDecimal;

public record CategoryScoreResult(String categoryKey, BigDecimal categoryScore, RiskLevel categoryLevel) {
}
