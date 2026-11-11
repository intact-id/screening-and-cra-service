package com.codify.sactionsservice.screening.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "yente",
        url = "${open-sanctions.yente.base-url}"
)
public interface YenteFeignClient extends OpenSanctionsApi {
}
