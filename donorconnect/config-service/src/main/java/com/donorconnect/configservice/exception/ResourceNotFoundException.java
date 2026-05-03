package com.donorconnect.configservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entity, Object id) {
        super(entity + " not found with id: " + id);
    }
    public ResourceNotFoundException(String entity, String field, Object value) {
        super(entity + " not found with " + field + ": " + value);
    }
}