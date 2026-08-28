package com.codify.sactionsservice.risk.engine;

public record AttributeScoreResult(int points, String matchedRuleId, boolean incomplete, String note) {
}
