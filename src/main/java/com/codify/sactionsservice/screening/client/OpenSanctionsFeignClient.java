package com.codify.sactionsservice.screening.client;

import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "open-sanctions",
        url = "https://api.opensanctions.org",
        configuration = OpenSanctionsFeignConfig.class
)
public interface OpenSanctionsFeignClient {

    @PostMapping("/match/default")
    Map<String, Object> match(
            @RequestBody ScreeningBaseRequestDto screeningBaseRequestDto,
            @RequestParam("algorithm") String algorithm,
            @RequestParam("threshold") double threshold,
            @RequestParam("topics") List<String> topics
    );
}
