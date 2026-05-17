package com.donorconnect.billingservice.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Custom Billing Exceptions ───────────────────────────────────────────────

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<ErrorResponse> handleBillingException(BillingException ex) {
        ErrorResponse body = new ErrorResponse(
                ex.getStatus().value(),
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // ── Bean validation (@Valid) ────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        ValidationErrorResponse body = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors,
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ── Missing / wrong-type query params ───────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_PARAMETER",
                String.format("Required parameter '%s' of type '%s' is missing",
                        ex.getParameterName(), ex.getParameterType()),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "TYPE_MISMATCH",
                String.format("Parameter '%s' should be of type '%s'", ex.getName(), type),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(body);
    }

    // ── Unparseable JSON body / unknown enum value ──────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        String code    = "MALFORMED_REQUEST";
        String message = "Request body is malformed or contains invalid values";

        if (cause instanceof InvalidFormatException ife && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            code    = "INVALID_ENUM_VALUE";
            message = String.format("Invalid value '%s' for field '%s'. Allowed: %s",
                    ife.getValue(),
                    ife.getPath().isEmpty() ? "?" : ife.getPath().get(ife.getPath().size() - 1).getFieldName(),
                    java.util.Arrays.toString(ife.getTargetType().getEnumConstants()));
        }

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), code, message, LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    // ── Spring Security ─────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "You do not have permission to perform this action",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ── Fallback ────────────────────────────────────────────────────────────────

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Always log the real exception — silent 500s waste hours of debugging.
        log.error("Unhandled exception reached fallback handler: {}", ex.getMessage(), ex);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ── Response shapes ─────────────────────────────────────────────────────────

    public record ErrorResponse(
            int status,
            String errorCode,
            String message,
            LocalDateTime timestamp
    ) {}

    public record ValidationErrorResponse(
            int status,
            String errorCode,
            String message,
            Map<String, String> fieldErrors,
            LocalDateTime timestamp
    ) {}
}
