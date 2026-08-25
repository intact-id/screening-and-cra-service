package com.codify.sactionsservice.risk.repository;

import com.codify.sactionsservice.risk.domain.riskassessment.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepo extends JpaRepository<RiskAssessment, String> {

    // "Current risk level" = the level of the latest completed assessment —
    // derived by query, never stored as separate mutable state (cra-data-model.md §5.3).
    Optional<RiskAssessment> findTopByTenant_IdAndCustomerIdOrderByCreatedAtDesc(
            String tenantId, String customerId);

    List<RiskAssessment> findByTenant_IdAndCustomerIdOrderByCreatedAtDesc(
            String tenantId, String customerId);
}
