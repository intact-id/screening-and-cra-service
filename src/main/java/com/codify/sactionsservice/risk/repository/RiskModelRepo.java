package com.codify.sactionsservice.risk.repository;

import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.domain.riskmodel.LifeCycleStatus;
import com.codify.sactionsservice.risk.domain.riskmodel.RiskModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskModelRepo extends JpaRepository<RiskModel, String> {

    Optional<RiskModel> findByTenant_IdAndCustomerTypeAndLifeCycleStatus(
            String tenantId, CustomerType customerType, LifeCycleStatus lifeCycleStatus);

    Optional<RiskModel> findTopByTenant_IdAndCustomerTypeOrderByVersionDesc(
            String tenantId, CustomerType customerType);
}
