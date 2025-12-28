package com.ecommerce.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(
            PaymentNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Payment Not Found",
            ex.getMessage(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessingException(
            PaymentProcessingException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Payment Processing Error",
            ex.getMessage(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        if (ex.getErrorCode() != null) {
            errorResponse.addDetail("errorCode", ex.getErrorCode());
        }
        if (ex.getPaymentId() != null) {
            errorResponse.addDetail("paymentId", ex.getPaymentId());
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStateException(
            InvalidPaymentStateException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Invalid Payment State",
            ex.getMessage(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        if (ex.getPaymentId() != null) {
            errorResponse.addDetail("paymentId", ex.getPaymentId());
        }
        if (ex.getCurrentStatus() != null) {
            errorResponse.addDetail("currentStatus", ex.getCurrentStatus().toString());
        }
        if (ex.getOperation() != null) {
            errorResponse.addDetail("operation", ex.getOperation());
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Request validation failed",
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorResponse.addValidationError(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getDescription(false),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // Error response classes
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private String path;
        private LocalDateTime timestamp;
        private Map<String, Object> details;
        
        public ErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
            this.details = new HashMap<>();
        }
        
        public void addDetail(String key, Object value) {
            details.put(key, value);
        }
        
        // Getters
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getDetails() { return details; }
    }
    
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> validationErrors;
        
        public ValidationErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {
            super(status, error, message, path, timestamp);
            this.validationErrors = new HashMap<>();
        }
        
        public void addValidationError(String field, String message) {
            validationErrors.put(field, message);
        }
        
        public Map<String, String> getValidationErrors() { return validationErrors; }
    }
}