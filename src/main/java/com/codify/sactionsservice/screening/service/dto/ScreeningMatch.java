package com.codify.sactionsservice.screening.service.dto;

import java.util.List;

public record ScreeningMatch(
        String entityId,
        String caption,
        double score,
        List<String> datasets,
        List<String> topics,
        List<String> sourceUrls
) {
}
