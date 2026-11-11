package com.codify.sactionsservice.screening.api;

import com.codify.sactionsservice.screening.client.ScreeningClient;
import com.codify.sactionsservice.screening.client.ScreeningEngine;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.codify.sactionsservice.screening.client.constant.EntitySchema;
import com.codify.sactionsservice.screening.client.constant.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Read-only lookups against the screening engines, returned as the engine sends them.
 * Use these to investigate a match: search by name, open the matched entity, check which
 * datasets (and versions) the engine has loaded, and list its algorithms.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/screening")
@RequiredArgsConstructor
public class ScreeningLookupController {

    private static final String ENGINE_DOC = "Engine to query. Omit to use the hosted OpenSanctions API.";
    private final ScreeningClient screeningClient;

    @Operation(summary = "Free-text search of sanctions and PEP entities (not supported by MOTIVA)")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @Parameter(description = "Name or text to search for", example = "William Ruto")
            @RequestParam String q,
            @Parameter(description = "Entity type")
            @RequestParam(required = false) EntitySchema schema,
            @Parameter(description = "ISO country codes, e.g. ke (free text: there are ~250 codes)")
            @RequestParam(required = false) List<String> countries,
            @Parameter(description = "Only return entities tagged with these topics")
            @RequestParam(required = false) List<Topic> topics,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @Parameter(description = ENGINE_DOC)
            @RequestParam(required = false) ScreeningEngine engine) {
        return ResponseEntity.ok(screeningClient.search(q, schema, countries, topics, limit, offset, engine));
    }

    @Operation(summary = "Fetch one entity with its full properties, sanctions and related entities")
    @GetMapping("/entities/{id}")
    public ResponseEntity<Map<String, Object>> entity(
            @Parameter(description = "Entity id from a match or search result", example = "Q7747")
            @PathVariable String id,
            @Parameter(description = ENGINE_DOC)
            @RequestParam(required = false) ScreeningEngine engine) {
        return ResponseEntity.ok(screeningClient.entity(id, engine));
    }

    @Operation(summary = "Datasets the engine has loaded, with their versions (shows how fresh the data is)")
    @GetMapping("/catalog")
    public ResponseEntity<Map<String, Object>> catalog(
            @Parameter(description = ENGINE_DOC)
            @RequestParam(required = false) ScreeningEngine engine) {
        return ResponseEntity.ok(screeningClient.catalog(engine));
    }

    @Operation(summary = "Matching algorithms the engine supports")
    @GetMapping("/algorithms")
    public ResponseEntity<Map<String, Object>> algorithms(
            @Parameter(description = ENGINE_DOC)
            @RequestParam(required = false) ScreeningEngine engine) {
        return ResponseEntity.ok(screeningClient.algorithms(engine));
    }

    /** Pass the engine's status through (e.g. 404 for an unknown entity) instead of a 500. */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> engineError(FeignException e) {
        int status = e.status() > 0 ? e.status() : HttpStatus.BAD_GATEWAY.value();
        log.warn("Screening engine call failed: status={} message={}", status, e.getMessage());
        String message = status == 404 ? "Not found on the screening engine" : "Screening engine request failed";
        // Surface the engine's own reason (e.g. {"detail":"Invalid schema"}) so callers can fix their request.
        String detail = e.contentUTF8();
        return ResponseEntity.status(status).body(Map.of(
                "status", "FAILED",
                "message", message,
                "engineResponse", detail == null || detail.isBlank() ? "" : detail));
    }

    /** An unknown enum value (e.g. schema=banana) gets a 400 naming the parameter. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> badParameter(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "FAILED",
                "message", "Invalid value '" + e.getValue() + "' for parameter '" + e.getName() + "'"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> badRequest(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("status", "FAILED", "message", e.getReason()));
    }
}
