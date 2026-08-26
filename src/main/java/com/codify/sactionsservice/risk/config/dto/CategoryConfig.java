package com.codify.sactionsservice.risk.config.dto;

import com.codify.sactionsservice.risk.domain.category.RollUpMethod;
import com.codify.sactionsservice.risk.domain.category.Timing;

import java.math.BigDecimal;
import java.util.List;

public record CategoryConfig(
        String key,
        String displayName,
        BigDecimal weight,
        RollUpMethod rollupMethod,
        Timing timing,
        Integer ordering,
        List<ThresholdConfig> thresholds,
        List<AttributeConfig> attributes
) {
}
