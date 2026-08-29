package com.codify.sactionsservice.risk.service;

import com.codify.sactionsservice.common.FieldError;
import com.codify.sactionsservice.common.exception.ApplicationException;
import com.codify.sactionsservice.risk.api.dto.AssessmentRequestDto;
import com.codify.sactionsservice.risk.api.dto.AssessmentResponseDto;
import com.codify.sactionsservice.risk.api.mapper.AssessmentResponseMapper;
import com.codify.sactionsservice.risk.config.mapper.ScoringResultMapper;
import com.codify.sactionsservice.risk.domain.ReservedAttributeKeys;
import com.codify.sactionsservice.risk.domain.riskassessment.RiskAssessment;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.risk.engine.AttributePayload;
import com.codify.sactionsservice.risk.engine.RiskModelSnapshot;
import com.codify.sactionsservice.risk.engine.ScoringPipeline;
import com.codify.sactionsservice.risk.engine.ScoringResult;
import com.codify.sactionsservice.risk.repository.RiskAssessmentRepo;
import com.codify.sactionsservice.risk.repository.RiskModelRepo;
import com.codify.sactionsservice.screening.service.BusinessMatchService;
import com.codify.sactionsservice.screening.service.PersonMatchService;
import com.codify.sactionsservice.screening.service.dto.ScreeningResult;
import com.codify.sactionsservice.screening.service.dto.ScreeningVerdict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final RiskModelService riskModelService;
    private final RiskModelRepo riskModelRepo;
    private final RiskAssessmentRepo riskAssessmentRepo;
    private final ScoringResultMapper scoringResultMapper;
    private final AssessmentResponseMapper responseMapper;
    private final PersonMatchService personMatchService;
    private final BusinessMatchService businessMatchService;

    @Transactional
    public AssessmentResponseDto runAssessment(AssessmentRequestDto request) {
        RiskModelSnapshot snapshot = riskModelService.getActive(request.tenantId(), request.customerType());

        RiskModel riskModel = riskModelRepo.findById(snapshot.riskModelId())
                .orElseThrow(() -> {
                    log.error("Active RiskModelSnapshot referenced a model that no longer exists: riskModelId={}",
                            snapshot.riskModelId());
                    return new ApplicationException.ResourceNotFoundException("Active risk model not found");
                });

        ScreeningResult screeningResult = screenCustomer(request);
        logScreeningOutcome(request, screeningResult);

        Map<String, Object> payload = new HashMap<>(request.attributePayload());
        payload.put(ReservedAttributeKeys.SANCTIONS, screeningResult.verdict().name());
        payload.put(ScreeningAttributeMapper.PEP_ATTRIBUTE_KEY, ScreeningAttributeMapper.isPep(screeningResult));
        payload.put(ScreeningAttributeMapper.ADVERSE_MEDIA_ATTRIBUTE_KEY, ScreeningAttributeMapper.isAdverseMedia(screeningResult));

        ScoringResult result = ScoringPipeline.run(snapshot, new AttributePayload(payload), request.stage());

        RiskAssessment assessment = scoringResultMapper.toEntity(
                riskModel.getTenant(), riskModel, request.customerId(), request.customerType(), request.stage(),
                request.triggeredBy(), payload, result, screeningResult);

        RiskAssessment saved = riskAssessmentRepo.save(assessment);

        log.info("Ran assessment id={} tenantId={} customerId={} overallLevel={} overrideApplied={} incomplete={} screeningVerdict={}",
                saved.getId(), request.tenantId(), request.customerId(), result.overallLevel(),
                result.overrideApplied(), result.incomplete(), screeningResult.verdict());

        return responseMapper.toDto(saved, true);
    }

    @Transactional(readOnly = true)
    public AssessmentResponseDto getAssessment(String id, boolean includeBreakdown) {
        RiskAssessment assessment = riskAssessmentRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("RiskAssessment not found: id={}", id);
                    return new ApplicationException.ResourceNotFoundException("Assessment not found");
                });

        return responseMapper.toDto(assessment, includeBreakdown);
    }

    private ScreeningResult screenCustomer(AssessmentRequestDto request) {
        return switch (request.customerType()) {
            case INDIVIDUAL -> {
                if (request.personIdentity() == null) {
                    throw new ApplicationException.ValidationException(
                            "Screening identity is required",
                            List.of(new FieldError("personIdentity", "personIdentity is required for INDIVIDUAL assessments")));
                }
                yield personMatchService.match(request.personIdentity());
            }
            case BUSINESS -> {
                if (request.businessIdentity() == null) {
                    throw new ApplicationException.ValidationException(
                            "Screening identity is required",
                            List.of(new FieldError("businessIdentity", "businessIdentity is required for BUSINESS assessments")));
                }
                yield businessMatchService.match(request.businessIdentity());
            }
            case BOTH -> throw new IllegalStateException("CustomerType.BOTH is not valid on an assessment");
        };
    }

    private void logScreeningOutcome(AssessmentRequestDto request, ScreeningResult screeningResult) {
        if (screeningResult.verdict() == ScreeningVerdict.CONFIRMED_MATCH) {
            log.warn("Confirmed sanctions match on assessment: tenantId={} customerId={} stage={}",
                    request.tenantId(), request.customerId(), request.stage());
        } else if (screeningResult.verdict() == ScreeningVerdict.POTENTIAL_MATCH) {
            log.warn("Potential sanctions match on assessment — routing to EDD queue: tenantId={} customerId={} stage={}",
                    request.tenantId(), request.customerId(), request.stage());
        }
    }
}
