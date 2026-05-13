package com.donorconnect.billingservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a non-ADMIN user attempts to access a billing endpoint.
 * Maps to HTTP 403 Forbidden.
 */
public class BillingAccessDeniedException extends BillingException {

    public BillingAccessDeniedException() {
        super(
            "Access denied: only ADMIN users can access billing records",
            HttpStatus.FORBIDDEN,
            "BILLING_ACCESS_DENIED"
        );
    }
}
