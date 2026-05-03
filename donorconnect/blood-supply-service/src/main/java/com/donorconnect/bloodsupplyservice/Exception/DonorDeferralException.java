package com.donorconnect.bloodsupplyservice.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DonorDeferralException extends RuntimeException {
    private final String deferralReason;

    public DonorDeferralException(String message, String deferralReason) {
        super(message);
        this.deferralReason = deferralReason;
    }

    public String getDeferralReason() {
        return deferralReason;
    }
}
