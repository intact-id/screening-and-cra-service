package com.codify.sactionsservice.screening.service;

import com.codify.sactionsservice.screening.service.dto.ScreeningMatch;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import com.codify.sactionsservice.screening.service.dto.ScreeningVerdict;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ScreeningResultMapper {

    public static final double CONFIRMED_MATCH_SCORE_THRESHOLD = 1.0;

    public ScreeningResult toScreeningResult(Map<String, Object> rawResponse) {
        List<Map<String, Object>> results = extractResults(rawResponse);

        List<ScreeningMatch> matches = results.stream()
                .filter(result -> Boolean.TRUE.equals(result.get("match")))
                .map(this::toScreeningMatch)
                .toList();

        return new ScreeningResult(determineVerdict(matches), matches);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractResults(Map<String, Object> rawResponse) {
        if (!(rawResponse.get("responses") instanceof Map<?, ?> responses) || responses.isEmpty()) {
            return List.of();
        }

        Object firstQueryResponse = responses.values().iterator().next();
        if (!(firstQueryResponse instanceof Map<?, ?> queryResponse)) {
            return List.of();
        }

        if (!(queryResponse.get("results") instanceof List<?> results)) {
            return List.of();
        }

        return (List<Map<String, Object>>) (List<?>) results;
    }

    @SuppressWarnings("unchecked")
    private ScreeningMatch toScreeningMatch(Map<String, Object> result) {
        Map<String, Object> properties = result.get("properties") instanceof Map<?, ?> props
                ? (Map<String, Object>) props
                : Map.of();

        return new ScreeningMatch(
                asString(result.get("id")),
                asString(result.get("caption")),
                asDouble(result.get("score")),
                asStringList(result.get("datasets")),
                asStringList(properties.get("topics")),
                asStringList(properties.get("sourceUrl"))
        );
    }

    private ScreeningVerdict determineVerdict(List<ScreeningMatch> matches) {
        if (matches.isEmpty()) {
            return ScreeningVerdict.NO_HIT;
        }

        boolean hasConfirmedMatch = matches.stream()
                .anyMatch(match -> match.score() >= CONFIRMED_MATCH_SCORE_THRESHOLD);

        return hasConfirmedMatch ? ScreeningVerdict.CONFIRMED_MATCH : ScreeningVerdict.POTENTIAL_MATCH;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private double asDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    private List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(Object::toString).toList();
    }
}
