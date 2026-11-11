package com.codify.sactionsservice.screening.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "open-sanctions",
        url = "${open-sanctions.base-url}",
        configuration = OpenSanctionsFeignConfig.class
)
public interface OpenSanctionsFeignClient extends OpenSanctionsApi {
}
