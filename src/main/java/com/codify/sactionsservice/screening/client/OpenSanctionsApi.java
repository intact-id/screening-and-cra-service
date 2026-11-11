package com.codify.sactionsservice.screening.client;

import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * The OpenSanctions HTTP API, shared by every engine's Feign client.
 * Feign allows a client only one parent interface, so all endpoints live here.
 * Motiva has no /search; {@link ScreeningClient} rejects that call before it is sent.
 */
public interface OpenSanctionsApi {

    @PostMapping("/match/default")
    Map<String, Object> match(
            @RequestBody ScreeningBaseRequestDto screeningBaseRequestDto,
            @RequestParam("algorithm") String algorithm,
            @RequestParam("threshold") double threshold,
            @RequestParam("topics") List<String> topics
    );

    /** Free-text search. Null parameters are left out of the query string. */
    @GetMapping("/search/default")
    Map<String, Object> search(
            @RequestParam("q") String query,
            @RequestParam(value = "schema", required = false) String schema,
            @RequestParam(value = "countries", required = false) List<String> countries,
            @RequestParam(value = "topics", required = false) List<String> topics,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset
    );

    @GetMapping("/entities/{id}")
    Map<String, Object> entity(@PathVariable("id") String id);

    @GetMapping("/catalog")
    Map<String, Object> catalog();

    @GetMapping("/algorithms")
    Map<String, Object> algorithms();
}
