package com.donorconnect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientStockException extends RuntimeException {

    private final Long componentId;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long componentId, int requested, int available) {
        super("Insufficient stock for component " + componentId +
              ": requested " + requested + ", available " + available);
        this.componentId = componentId;
        this.requested   = requested;
        this.available   = available;
    }

    public Long getComponentId() { return componentId; }
    public int  getRequested()   { return requested; }
    public int  getAvailable()   { return available; }
}
