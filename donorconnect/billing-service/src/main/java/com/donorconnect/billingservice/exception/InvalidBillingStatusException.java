package com.donorconnect.billingservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an invalid or unrecognised billing status value is provided,
 * or when a status transition is not permitted (e.g. PAID → PENDING).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidBillingStatusException extends BillingException {

    public InvalidBillingStatusException(String providedStatus) {
        super(
            String.format("Invalid billing status: '%s'. Allowed values: PENDING, PAID, CANCELLED, OVERDUE",
                providedStatus),
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
