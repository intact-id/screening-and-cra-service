package com.codify.sactionsservice.tenant.repository;

import com.codify.sactionsservice.tenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepo extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByTenantIdentifier(String tenantIdentifier);
}
