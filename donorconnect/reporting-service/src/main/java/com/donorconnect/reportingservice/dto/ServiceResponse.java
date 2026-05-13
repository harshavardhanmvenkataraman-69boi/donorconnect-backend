package com.donorconnect.reportingservice.dto;

import lombok.Data;

@Data
public class ServiceResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
