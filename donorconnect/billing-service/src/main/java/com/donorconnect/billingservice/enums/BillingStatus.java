package com.donorconnect.billingservice.enums;

/**
 * Lifecycle states for a billing reference, per the design spec.
 *
 *  PENDING   — record created; awaiting export
 *  EXPORTED  — included in an export batch sent to the hospital billing system
 *  CANCELLED — voided; will not be exported again
 */
public enum BillingStatus {
    PENDING,
    EXPORTED,
    CANCELLED
}
