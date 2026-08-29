package com.codify.sactionsservice.risk.service;

import com.codify.sactionsservice.screening.service.dto.ScreeningMatch;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;

import java.util.Set;
import java.util.function.Predicate;

public final class ScreeningAttributeMapper {

    public static final String PEP_ATTRIBUTE_KEY = "pep";
    public static final String ADVERSE_MEDIA_ATTRIBUTE_KEY = "adverse_media";

    private static final String PEP_TOPIC = "role.pep";
    private static final Set<String> ADVERSE_MEDIA_TOPICS = Set.of("sanction", "sanction.linked", "debarment", "poi", "wanted");

    private ScreeningAttributeMapper() {
    }

    public static boolean isPep(ScreeningResult screeningResult) {
        return hasTopic(screeningResult, PEP_TOPIC::equals);
    }

    public static boolean isAdverseMedia(ScreeningResult screeningResult) {
        return hasTopic(screeningResult, topic -> ADVERSE_MEDIA_TOPICS.contains(topic) || topic.startsWith("crime."));
    }

    private static boolean hasTopic(ScreeningResult screeningResult, Predicate<String> predicate) {
        return screeningResult.matches().stream()
                .flatMap(match -> match.topics().stream())
                .anyMatch(predicate);
    }
}
