package com.donorconnect.billingservice.enums;

/**
 * Charge categories supported by the billing service.
 *
 *  PROCESSING — fee for preparing the blood component
 *  SERVICE    — facility / service charges
 *  CROSSMATCH — pre-issue compatibility testing
 */
public enum ChargeType {
    PROCESSING,
    SERVICE,
    CROSSMATCH
}
