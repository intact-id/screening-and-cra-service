package com.codify.sactionsservice.screening.client;

/**
 * Matching backend used for a screening request. All three speak the OpenSanctions /match API.
 */
public enum ScreeningEngine {
    /** Hosted api.opensanctions.org (paid, API key). The default when no engine is chosen. */
    OPENSANCTIONS,
    /** Self-hosted OpenSanctions yente server. */
    YENTE,
    /** Self-hosted motiva: lightweight Rust re-implementation of the yente match API. */
    MOTIVA
}
