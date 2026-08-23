package com.codify.sactionsservice.screening.client.request;

import com.codify.sactionsservice.screening.client.dto.Identity;
import com.codify.sactionsservice.screening.client.constant.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningBaseRequestDto {

    @JsonProperty("queries")
    private Map<String, BaseRequest> queries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseRequest {

        @JsonProperty("schema")
        private Schema schema;

        @JsonProperty("properties")
        private Identity properties;
    }
}
