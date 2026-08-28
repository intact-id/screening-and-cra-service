package com.codify.sactionsservice.risk.service;

import com.codify.sactionsservice.common.exception.ApplicationException;
import com.codify.sactionsservice.risk.config.dto.RiskModelConfig;
import com.codify.sactionsservice.risk.config.mapper.RiskModelConfigMapper;
import com.codify.sactionsservice.risk.config.mapper.RiskModelSnapshotMapper;
import com.codify.sactionsservice.risk.config.mapper.RiskModelToConfigMapper;
import com.codify.sactionsservice.risk.config.validator.RiskModelConfigValidator;
import com.codify.sactionsservice.risk.config.validator.ValidationResult;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.domain.riskmodel.LifeCycleStatus;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import com.codify.sactionsservice.risk.engine.RiskModelSnapshot;
import com.codify.sactionsservice.risk.repository.RiskModelRepo;
import com.codify.sactionsservice.tenant.domain.Tenant;
import com.codify.sactionsservice.tenant.repository.TenantRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskModelService {

    private final RiskModelRepo riskModelRepo;
    private final TenantRepo tenantRepo;
    private final RiskModelConfigValidator validator;
    private final RiskModelConfigMapper configMapper;
    private final RiskModelToConfigMapper toConfigMapper;
    private final RiskModelSnapshotMapper snapshotMapper;

    @Transactional
    public RiskModel createDraft(String tenantId, RiskModelConfig config) {
        ValidationResult result = validator.validateForDraft(config);
        if (!result.isValid()) {
            log.warn("Rejected RiskModel draft: tenantId={} errors={}", tenantId, result.errors());
            throw new ApplicationException.ValidationException(
                    "Risk model configuration is invalid: " + result.errors());
        }

        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> {
                    log.warn("Tenant not found: tenantId={}", tenantId);
                    return new ApplicationException.ResourceNotFoundException("Tenant not found");
                });

        Integer nextVersion = riskModelRepo.findTopByTenant_IdAndCustomerTypeOrderByVersionDesc(tenantId, config.customerType())
                .map(RiskModel::getVersion)
                .map(version -> version + 1)
                .orElse(1);

        RiskModel riskModel = configMapper.toEntity(config, tenant, nextVersion);
        RiskModel saved = riskModelRepo.save(riskModel);

        log.info("Created RiskModel draft id={} tenantId={} customerType={} version={}",
                saved.getId(), tenantId, config.customerType(), nextVersion);

        return saved;
    }

    @Transactional
    public RiskModel activate(String riskModelId) {
        RiskModel riskModel = riskModelRepo.findById(riskModelId)
                .orElseThrow(() -> {
                    log.warn("RiskModel not found: riskModelId={}", riskModelId);
                    return new ApplicationException.ResourceNotFoundException("Risk model not found");
                });

        if (riskModel.getLifeCycleStatus() != LifeCycleStatus.DRAFT) {
            log.warn("Refused to activate RiskModel not in DRAFT: riskModelId={} status={}",
                    riskModelId, riskModel.getLifeCycleStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a DRAFT risk model can be activated");
        }

        RiskModelConfig config = toConfigMapper.toConfig(riskModel);
        ValidationResult result = validator.validateForActivation(config);
        if (!result.isValid()) {
            log.warn("Refused to activate RiskModel: riskModelId={} errors={}", riskModelId, result.errors());
            throw new ApplicationException.ValidationException(
                    "Risk model cannot be activated: " + result.errors());
        }

        riskModelRepo.findByTenant_IdAndCustomerTypeAndLifeCycleStatus(
                        riskModel.getTenant().getId(), riskModel.getCustomerType(), LifeCycleStatus.ACTIVE)
                .ifPresent(currentlyActive -> {
                    currentlyActive.setLifeCycleStatus(LifeCycleStatus.RETIRED);
                    riskModelRepo.save(currentlyActive);
                });

        riskModel.setLifeCycleStatus(LifeCycleStatus.ACTIVE);
        RiskModel saved = riskModelRepo.save(riskModel);

        log.info("Activated RiskModel id={} tenantId={} customerType={} version={}",
                saved.getId(), riskModel.getTenant().getId(), riskModel.getCustomerType(), riskModel.getVersion());

        return saved;
    }

    @Transactional(readOnly = true)
    public RiskModelSnapshot getActive(String tenantId, CustomerType customerType) {
        RiskModel riskModel = riskModelRepo.findByTenant_IdAndCustomerTypeAndLifeCycleStatus(
                        tenantId, customerType, LifeCycleStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("No ACTIVE risk model found: tenantId={} customerType={}", tenantId, customerType);
                    return new ApplicationException.ResourceNotFoundException("No active risk model found");
                });

        return snapshotMapper.toSnapshot(riskModel);
    }
}
