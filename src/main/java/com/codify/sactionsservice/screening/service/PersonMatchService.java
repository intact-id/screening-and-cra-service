package com.codify.sactionsservice.screening.service;

import com.codify.sactionsservice.screening.api.dto.PersonMatchDto;
import com.codify.sactionsservice.screening.client.OpenSanctionsFeignClient;
import com.codify.sactionsservice.screening.client.constant.Schema;
import com.codify.sactionsservice.screening.client.dto.PersonIdentity;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto.BaseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PersonMatchService {

    private static final String QUERY_KEY = "q";

    private final OpenSanctionsFeignClient openSanctionsFeignClient;

    @Value("${open-sanctions.algorithm}")
    private String algorithm;

    @Value("${open-sanctions.threshold}")
    private double threshold;

    @Value("${open-sanctions.topics}")
    private List<String> topics;

    public Map<String, Object> match(PersonMatchDto personMatchDto) {
        PersonIdentity identity = new PersonIdentity(
                personMatchDto.firstName(),
                personMatchDto.lastName(),
                personMatchDto.fatherName(),
                personMatchDto.birthDate()
        );

        BaseRequest baseRequest = BaseRequest.builder()
                .schema(Schema.PERSON)
                .properties(identity)
                .build();

        ScreeningBaseRequestDto request = ScreeningBaseRequestDto.builder()
                .queries(Map.of(QUERY_KEY, baseRequest))
                .build();

        return openSanctionsFeignClient.match(request, algorithm, threshold, topics);
    }
}
