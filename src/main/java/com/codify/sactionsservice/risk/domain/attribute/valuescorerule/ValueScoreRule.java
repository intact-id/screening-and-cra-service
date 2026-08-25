package com.codify.sactionsservice.risk.domain.attribute.valuescorerule;

import com.codify.sactionsservice.common.PO;
import com.codify.sactionsservice.common.converter.PgJsonbToMapConverter;
import com.codify.sactionsservice.risk.domain.attribute.Attribute;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ValueScoreRule extends PO {

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @Convert(converter = PgJsonbToMapConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "match_value", columnDefinition = "jsonb")
    private Map<String, Object> matchValue;


    private Integer priority;

    private Integer score;


}
