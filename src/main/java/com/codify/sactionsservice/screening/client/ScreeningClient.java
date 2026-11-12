package com.codify.sactionsservice.screening.client;

import com.codify.sactionsservice.screening.client.constant.EntitySchema;
import com.codify.sactionsservice.screening.client.constant.Topic;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Sends a match request to the chosen engine with that engine's algorithm.
 * Threshold and topics are shared, so every engine filters results the same way.
 * With no engine chosen, the configured default (the hosted OpenSanctions API) is used.
 */
@Component
@RequiredArgsConstructor
public class ScreeningClient {

    private final OpenSanctionsFeignClient openSanctionsClient;
    private final YenteFeignClient yenteClient;
    private final MotivaFeignClient motivaClient;

    @Value("${open-sanctions.default-engine:OPENSANCTIONS}")
    private ScreeningEngine defaultEngine;

    @Value("${open-sanctions.algorithm}")
    private String openSanctionsAlgorithm;

    @Value("${open-sanctions.yente.algorithm}")
    private String yenteAlgorithm;

    @Value("${open-sanctions.motiva.algorithm}")
    private String motivaAlgorithm;

    @Value("${open-sanctions.threshold}")
    private double threshold;

    @Value("${open-sanctions.topics}")
    private List<String> topics;

    public Map<String, Object> match(ScreeningBaseRequestDto request, ScreeningEngine engine) {
        return switch (resolve(engine)) {
            case OPENSANCTIONS -> openSanctionsClient.match(request, openSanctionsAlgorithm, threshold, topics);
            case YENTE -> yenteClient.match(request, yenteAlgorithm, threshold, topics);
            case MOTIVA -> motivaClient.match(request, motivaAlgorithm, threshold, topics);
        };
    }

    public Map<String, Object> search(String query, EntitySchema schema, List<String> countries,
                                      List<Topic> searchTopics, Integer limit, Integer offset,
                                      ScreeningEngine engine) {
        // The engines take the exact schema name ("Person") and topic id ("role.pep").
        String schemaValue = schema == null ? null : schema.getValue();
        List<String> topicValues = searchTopics == null ? null : searchTopics.stream().map(Topic::getValue).toList();
        return switch (resolve(engine)) {
            case OPENSANCTIONS -> openSanctionsClient.search(query, schemaValue, countries, topicValues, limit, offset);
            case YENTE -> yenteClient.search(query, schemaValue, countries, topicValues, limit, offset);
            case MOTIVA -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Motiva has no free-text search; use engine=YENTE or OPENSANCTIONS");
        };
    }

    public Map<String, Object> entity(String id, ScreeningEngine engine) {
        return lookup(engine).entity(id);
    }

    public Map<String, Object> catalog(ScreeningEngine engine) {
        return lookup(engine).catalog();
    }

    public Map<String, Object> algorithms(ScreeningEngine engine) {
        return lookup(engine).algorithms();
    }

    private OpenSanctionsApi lookup(ScreeningEngine engine) {
        return switch (resolve(engine)) {
            case OPENSANCTIONS -> openSanctionsClient;
            case YENTE -> yenteClient;
            case MOTIVA -> motivaClient;
        };
    }

    private ScreeningEngine resolve(ScreeningEngine engine) {
        return engine != null ? engine : defaultEngine;
    }
}
