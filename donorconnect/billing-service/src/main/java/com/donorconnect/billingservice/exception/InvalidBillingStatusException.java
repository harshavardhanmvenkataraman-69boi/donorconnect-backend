package com.donorconnect.billingservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an unknown billing status is provided, or a status
 * transition is not permitted by the workflow rules.
 *
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidBillingStatusException extends BillingException {

    private static final String ALLOWED = "PENDING, EXPORTED, CANCELLED";

    public InvalidBillingStatusException(String providedStatus) {
        super(
            String.format("Invalid billing status: '%s'. Allowed values: %s",
                providedStatus, ALLOWED),
            HttpStatus.BAD_REQUEST,
            "INVALID_BILLING_STATUS"
        );
    }

    public InvalidBillingStatusException(String currentStatus, String targetStatus) {
        super(
            String.format("Cannot transition billing status from '%s' to '%s'",
                currentStatus, targetStatus),
            HttpStatus.BAD_REQUEST,
            "INVALID_STATUS_TRANSITION"
        );
    }
}
