package com.codify.sactionsservice.risk.engine;

import com.codify.sactionsservice.risk.domain.category.RollUpMethod;
import com.codify.sactionsservice.risk.domain.category.Timing;

import java.math.BigDecimal;
import java.util.List;

public record CategorySnapshot(
        String key,
        BigDecimal weight,
        RollUpMethod rollupMethod,
        Timing timing,
        List<ThresholdBand> thresholds,
        List<AttributeSnapshot> attributes
) {
}
