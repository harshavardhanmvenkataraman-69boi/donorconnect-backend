package com.donorconnect.bloodsupplyservice.Exception;

import com.donorconnect.bloodsupplyservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<?>> handleServiceUnavailableException(ServiceUnavailableException e) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(e.getMessage(), "SERVICE_UNAVAILABLE", e.getServiceName()));
    }

    @ExceptionHandler(AppointmentStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleAppointmentStatusException(AppointmentStatusException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "APPOINTMENT_STATUS_ERROR", e.getAppointmentStatus()));
    }

    @ExceptionHandler(DonorDeferralException.class)
    public ResponseEntity<ApiResponse<?>> handleDonorDeferralException(DonorDeferralException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage(), "DONOR_DEFERRED", e.getDeferralReason()));
    }

    @ExceptionHandler(DonationDateValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleDonationDateValidationException(DonationDateValidationException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "DATE_VALIDATION_ERROR", null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), "RESOURCE_NOT_FOUND", e.getResourceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage(), "INTERNAL_ERROR", null));
    }
}
