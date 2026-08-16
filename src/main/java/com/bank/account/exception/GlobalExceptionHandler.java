package com.bank.account.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralized exception translation to HTTP responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles domain {@link BusinessException} instances.
     *
     * @param ex       business exception
     * @param exchange current exchange
     * @return error response entity
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, ServerWebExchange exchange) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return build(ex.getStatus(), ex.getMessage(), exchange);
    }

    /**
     * Handles bean validation failures.
     *
     * @param ex       bind exception
     * @param exchange current exchange
     * @return error response entity
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
        String message = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", message);
        return build(HttpStatus.BAD_REQUEST, message, exchange);
    }

    /**
     * Handles unexpected failures.
     *
     * @param ex       unexpected exception
     * @param exchange current exchange
     * @return error response entity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", exchange);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, ServerWebExchange exchange) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
