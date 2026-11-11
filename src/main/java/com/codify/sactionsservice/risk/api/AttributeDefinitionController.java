package com.codify.sactionsservice.risk.api;

import com.codify.sactionsservice.common.ApiResult;
import com.codify.sactionsservice.common.web.ControllerUtil;
import com.codify.sactionsservice.risk.api.dto.AttributeDefinitionResponseDto;
import com.codify.sactionsservice.risk.domain.riskmodel.CustomerType;
import com.codify.sactionsservice.risk.service.AttributeDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/attribute-definitions", produces = "application/json")
public class AttributeDefinitionController extends ControllerUtil {

    private final AttributeDefinitionService attributeDefinitionService;

    @GetMapping
    public ResponseEntity<ApiResult<List<AttributeDefinitionResponseDto>>> list(
            @RequestParam(required = false) CustomerType customerType,
            @RequestParam(required = false) String categoryKey) {
        return handleGet(
                () -> attributeDefinitionService.list(customerType, categoryKey),
                log,
                String.valueOf(customerType),
                "Attribute definitions fetched successfully",
                "attribute definitions not found: customerType={} reason={}",
                "failed to list attribute definitions: customerType={} reason={}"
        );
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResult<AttributeDefinitionResponseDto>> getByKey(@PathVariable String key) {
        return handleGet(
                () -> attributeDefinitionService.getByKey(key),
                log,
                key,
                "Attribute definition fetched successfully",
                "attribute definition not found: key={} reason={}",
                "failed to fetch attribute definition: key={} reason={}"
        );
    }
}
