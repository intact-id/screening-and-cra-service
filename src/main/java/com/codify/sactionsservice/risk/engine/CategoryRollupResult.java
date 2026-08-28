package com.codify.sactionsservice.risk.engine;

import java.util.List;

public record CategoryRollupResult(CategoryScoreResult categoryScore, List<ContributionLineResult> contributionLines) {
}
