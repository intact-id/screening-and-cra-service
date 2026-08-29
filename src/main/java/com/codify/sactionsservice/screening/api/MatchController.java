package com.codify.sactionsservice.screening.api;

import com.codify.sactionsservice.screening.api.dto.BusinessMatchDto;
import com.codify.sactionsservice.screening.api.dto.PersonMatchDto;
import com.codify.sactionsservice.screening.service.BusinessMatchService;
import com.codify.sactionsservice.screening.service.PersonMatchService;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final BusinessMatchService businessMatchService;
    private final PersonMatchService personMatchService;

    @PostMapping("/business")
    public ResponseEntity<ScreeningResult> matchBusiness(@RequestBody BusinessMatchDto businessMatchDto) {
        return ResponseEntity.ok(businessMatchService.match(businessMatchDto));
    }

    @PostMapping("/person")
    public ResponseEntity<ScreeningResult> matchPerson(@RequestBody PersonMatchDto personMatchDto) {
        return ResponseEntity.ok(personMatchService.match(personMatchDto));
    }

}
