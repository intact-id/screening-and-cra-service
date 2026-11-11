package com.codify.sactionsservice.screening.service;

import com.codify.sactionsservice.screening.api.dto.BusinessMatchDto;
import com.codify.sactionsservice.screening.client.ScreeningClient;
import com.codify.sactionsservice.screening.client.ScreeningEngine;
import com.codify.sactionsservice.screening.client.constant.Schema;
import com.codify.sactionsservice.screening.client.dto.BusinessIdentity;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto.BaseRequest;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessMatchService {

    private static final String QUERY_KEY = "q";

    private final ScreeningClient screeningClient;
    private final ScreeningResultMapper screeningResultMapper;

    /** Screens with the default engine (the hosted OpenSanctions API unless configured otherwise). */
    public ScreeningResult match(BusinessMatchDto businessMatchDto) {
        return match(businessMatchDto, null);
    }

    public ScreeningResult match(BusinessMatchDto businessMatchDto, ScreeningEngine engine) {
        BusinessIdentity identity = BusinessIdentity.of(
                businessMatchDto.name(),
                businessMatchDto.jurisdiction()
        );

        BaseRequest baseRequest = BaseRequest.builder()
                .schema(Schema.BUSINESS)
                .properties(identity)
                .build();

        ScreeningBaseRequestDto request = ScreeningBaseRequestDto.builder()
                .queries(Map.of(QUERY_KEY, baseRequest))
                .build();

        Map<String, Object> rawResponse = screeningClient.match(request, engine);

        return screeningResultMapper.toScreeningResult(rawResponse);
    }
}
