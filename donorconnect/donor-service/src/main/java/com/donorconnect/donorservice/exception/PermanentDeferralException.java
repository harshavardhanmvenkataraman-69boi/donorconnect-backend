package com.donorconnect.donorservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PermanentDeferralException extends RuntimeException {

    private final Long deferralId;

    public PermanentDeferralException(Long deferralId) {
        super("Cannot lift permanent deferral with id: " + deferralId);
        this.deferralId = deferralId;
    }

    public Long getDeferralId() { return deferralId; }
}
