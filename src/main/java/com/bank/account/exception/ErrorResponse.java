package com.bank.account.exception;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Standard error body returned by the API.
 */
@Value
@Builder
public class ErrorResponse {

    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
}
