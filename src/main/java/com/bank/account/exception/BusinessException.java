package com.bank.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Domain exception carrying an HTTP status for REST error mapping.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Creates a business exception.
     *
     * @param message human-readable error message
     * @param status  HTTP status to return
     */
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
