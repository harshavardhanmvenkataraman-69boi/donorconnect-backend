package com.donorconnect.billingservice.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;

/**
 * Thrown when the 'from' date is after the 'to' date in the export endpoint.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidDateRangeException extends BillingException {

    public InvalidDateRangeException(LocalDate from, LocalDate to) {
        super(
            String.format("Invalid date range: 'from' date (%s) must not be after 'to' date (%s)",
                from, to),
            HttpStatus.BAD_REQUEST,
            "INVALID_DATE_RANGE"
        );
    }
}
