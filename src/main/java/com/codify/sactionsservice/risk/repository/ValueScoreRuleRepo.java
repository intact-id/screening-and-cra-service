package com.codify.sactionsservice.risk.repository;

import com.codify.sactionsservice.risk.domain.attribute.valuescorerule.ValueScoreRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValueScoreRuleRepo extends JpaRepository<ValueScoreRule, String> {
}
