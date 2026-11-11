package com.codify.sactionsservice.screening.service;

import com.codify.sactionsservice.screening.api.dto.PersonMatchDto;
import com.codify.sactionsservice.screening.client.ScreeningClient;
import com.codify.sactionsservice.screening.client.ScreeningEngine;
import com.codify.sactionsservice.screening.client.constant.Schema;
import com.codify.sactionsservice.screening.client.dto.PersonIdentity;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto;
import com.codify.sactionsservice.screening.client.request.ScreeningBaseRequestDto.BaseRequest;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PersonMatchService {

    private static final String QUERY_KEY = "q";

    private final ScreeningClient screeningClient;
    private final ScreeningResultMapper screeningResultMapper;

    /** Screens with the default engine (the hosted OpenSanctions API unless configured otherwise). */
    public ScreeningResult match(PersonMatchDto personMatchDto) {
        return match(personMatchDto, null);
    }

    public ScreeningResult match(PersonMatchDto personMatchDto, ScreeningEngine engine) {
        PersonIdentity identity = PersonIdentity.of(
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

        Map<String, Object> rawResponse = screeningClient.match(request, engine);

        return screeningResultMapper.toScreeningResult(rawResponse);
    }
}
