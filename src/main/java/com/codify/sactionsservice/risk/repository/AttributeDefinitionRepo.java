package com.codify.sactionsservice.risk.repository;

import com.codify.sactionsservice.risk.domain.attributedefinition.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeDefinitionRepo extends JpaRepository<AttributeDefinition, String> {

    Optional<AttributeDefinition> findByKey(String key);
}
