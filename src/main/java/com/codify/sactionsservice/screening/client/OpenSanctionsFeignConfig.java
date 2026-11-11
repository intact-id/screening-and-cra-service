package com.codify.sactionsservice.screening.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
/**
 * Feign configuration for the hosted OpenSanctions API only.
 * Deliberately not a @Configuration: a component-scanned Feign config applies to every
 * Feign client, which would send the API key to self-hosted yente and motiva too.
 */
public class OpenSanctionsFeignConfig {

    @Value("${open-sanctions.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor openSanctionsAuthInterceptor() {
        return requestTemplate -> requestTemplate.header("Authorization", "ApiKey " + apiKey);
    }
}
