package com.codify.sactionsservice.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResult<T> {
    private T data;
    private String status;
    private String message;
    private Boolean isTechnical;
    private List<FieldError> errors;
}
