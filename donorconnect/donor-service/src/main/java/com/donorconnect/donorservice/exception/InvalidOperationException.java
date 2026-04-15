package com.donorconnect.donorservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOperationException extends RuntimeException {

    private final String operation;

    public InvalidOperationException(String operation, String reason) {
        super("Operation '" + operation + "' is not allowed: " + reason);
        this.operation = operation;
    }

    public String getOperation() { return operation; }
}
