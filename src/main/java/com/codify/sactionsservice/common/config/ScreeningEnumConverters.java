package com.codify.sactionsservice.common.config;

import com.codify.sactionsservice.screening.client.constant.EntitySchema;
import com.codify.sactionsservice.screening.client.constant.Topic;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Lets query parameters bind these enums case-insensitively by constant name or engine value
 * (schema=person, topics=role.pep), instead of Spring's default exact-name match.
 */
@Configuration
public class ScreeningEnumConverters implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, EntitySchema.class, EntitySchema::from);
        registry.addConverter(String.class, Topic.class, Topic::from);
    }
}
