package com.codify.sactionsservice.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sactionsServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sactions Service API")
                        .description("Sanctions/PEP/adverse-media screening and Customer Risk Assessment engine")
                        .version("v1"));
    }
}
