package com.donorconnect.bloodsupplyservice.Exception;

public class DonationDateValidationException extends RuntimeException {
    public DonationDateValidationException(String message) {
        super(message);
    }
}
