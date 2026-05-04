package com.donorconnect.billingservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when attempting to create a billing record for an issue
 * that already has an existing billing entry.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateBillingException extends BillingException {

    public DuplicateBillingException(Integer issueId) {
        super(
            String.format("A billing record already exists for issue id: '%d'", issueId),
            HttpStatus.CONFLICT,
            "DUPLICATE_BILLING"
        );
    }
}
