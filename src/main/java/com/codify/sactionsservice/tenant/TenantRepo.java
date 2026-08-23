package com.codify.sactionsservice.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepo extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByTenantIdentifier(String tenantIdentifier);
}
