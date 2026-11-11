package com.codify.sactionsservice.screening.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "motiva",
        url = "${open-sanctions.motiva.base-url}"
)
public interface MotivaFeignClient extends OpenSanctionsApi {
}
