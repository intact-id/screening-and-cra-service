package com.codify.sactionsservice.screening.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSanctionsFeignConfig {

    @Value("${open-sanctions.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor openSanctionsAuthInterceptor() {
        return requestTemplate -> requestTemplate.header("Authorization", "ApiKey " + apiKey);
    }
}
