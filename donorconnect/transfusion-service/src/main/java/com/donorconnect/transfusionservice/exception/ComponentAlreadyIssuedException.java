package com.donorconnect.transfusionservice.exception;

public class ComponentAlreadyIssuedException extends RuntimeException {

    public ComponentAlreadyIssuedException(Long componentId, String currentStatus) {
        super("Component " + componentId + " cannot be issued. Current status: "
                + currentStatus + ". Component must be RESERVED before issuing.");
    }
    
}