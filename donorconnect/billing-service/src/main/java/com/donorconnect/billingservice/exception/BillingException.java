package com.donorconnect.billingservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all billing-related custom exceptions.
 * Carries an HTTP status and a machine-readable error code.
 */
public class BillingException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public BillingException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
