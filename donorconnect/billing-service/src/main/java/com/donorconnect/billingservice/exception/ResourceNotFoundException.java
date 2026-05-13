package com.donorconnect.billingservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a billing or issue record cannot be found by the given ID.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends BillingException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND"
        );
    }
}
