package com.codify.sactionsservice.screening.service;

import com.codify.sactionsservice.screening.api.dto.BusinessMatchDto;
import com.codify.sactionsservice.screening.client.OpenSanctionsFeignClient;
import com.codify.sactionsservice.screening.client.constant.Schema;
import com.codify.sactionsservice.screening.client.dto.BusinessIdentity;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto.BaseRequest;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessMatchService {

    private static final String QUERY_KEY = "q";

    private final OpenSanctionsFeignClient openSanctionsFeignClient;
    private final ScreeningResultMapper screeningResultMapper;

    @Value("${open-sanctions.algorithm}")
    private String algorithm;

    @Value("${open-sanctions.threshold}")
    private double threshold;

    @Value("${open-sanctions.topics}")
    private List<String> topics;

    public ScreeningResult match(BusinessMatchDto businessMatchDto) {
        BusinessIdentity identity = new BusinessIdentity(
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

        Map<String, Object> rawResponse = openSanctionsFeignClient.match(request, algorithm, threshold, topics);

        return screeningResultMapper.toScreeningResult(rawResponse);
    }
}
