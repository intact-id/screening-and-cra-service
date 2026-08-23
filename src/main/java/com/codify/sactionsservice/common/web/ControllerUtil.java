package com.codify.sactionsservice.common.web;

import com.codify.sactionsservice.common.ApiResult;
import com.codify.sactionsservice.common.exception.ApplicationException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

public abstract class ControllerUtil {

    public <T> ResponseEntity<ApiResult<T>> handleCreate(
            Supplier<T> action,
            Logger log,
            String successMessage,
            String validationLogMessage,
            String errorLogMessage
    ) {
        try {
            T data = action.get();
            return buildResponse(data, "SUCCESS", successMessage, Boolean.FALSE, HttpStatus.CREATED);

        } catch (ResponseStatusException e) {
            log.warn(validationLogMessage, e.getReason());
            return buildResponse(null, "FAILED", e.getReason(), false, HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (RuntimeException e) {
            log.warn(errorLogMessage, e.getMessage());
            return buildResponse(null, "FAILED", "An error occurred", true, HttpStatus.BAD_REQUEST);
        }

    }

    public <T> ResponseEntity<ApiResult<T>> handleUpdate(
            Supplier<T> action,
            Logger log,
            String referenceId,
            String successMessage,
            String notFoundLogMessage,
            String validationLogMessage,
            String errorLogMessage
    ) {
        try {
            T data = action.get();
            return buildResponse(data, "SUCCESS", successMessage, true, HttpStatus.OK);
        } catch (ApplicationException.ResourceNotFoundException e) {
            log.warn(notFoundLogMessage, referenceId, e.getMessage());
            return buildResponse(null, "FAILED", e.getMessage(), false, HttpStatus.NOT_FOUND);
        } catch (ResponseStatusException e) {
            log.warn(validationLogMessage, referenceId, e.getReason());
            return buildResponse(null, "FAILED", e.getReason(), false, HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (RuntimeException e) {
            log.error(errorLogMessage, referenceId, e.getMessage(), e);
            return buildResponse(null, "FAILED", "An error occurred", true, HttpStatus.BAD_REQUEST);
        }
    }


    public <T> ResponseEntity<ApiResult<T>> handleGet(
            Supplier<T> action,
            Logger log,
            String referenceId,
            String successMessage,
            String notFoundLogMessage,
            String errorLogMessage
    ) {
        try {
            T data = action.get();
            return buildResponse(data, "SUCCESS", successMessage, false, HttpStatus.OK);
        } catch (ApplicationException.ResourceNotFoundException e) {
            log.warn(notFoundLogMessage, referenceId, e.getMessage());
            return buildResponse(null, "FAILED", e.getMessage(), false, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            log.error(errorLogMessage, referenceId, e.getMessage(), e);
            return buildResponse(null, "FAILED", "An error occurred", true, HttpStatus.BAD_REQUEST);
        }
    }


    protected <T> ResponseEntity<ApiResult<T>> buildResponse(
            T data,
            String status,
            String message,
            boolean isTechnical,
            HttpStatus httpStatus
    ) {
        return ResponseEntity.status(httpStatus.value()).body(
                new ApiResult<>(data, status, message, isTechnical)
        );

    }

}
