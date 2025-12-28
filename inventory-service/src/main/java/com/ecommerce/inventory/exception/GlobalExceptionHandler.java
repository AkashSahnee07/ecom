package com.ecommerce.inventory.exception;

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
    
    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInventoryNotFoundException(
            InventoryNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVENTORY_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INSUFFICIENT_STOCK",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            "VALIDATION_FAILED",
            "Validation failed for request",
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false),
            LocalDateTime.now(),
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false),
            LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    // Error response classes
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private int status;
        private String path;
        private LocalDateTime timestamp;
        
        public ErrorResponse(String errorCode, String message, int status, String path, LocalDateTime timestamp) {
            this.errorCode = errorCode;
            this.message = message;
            this.status = status;
            this.path = path;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public String getPath() { return path; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> fieldErrors;
        
        public ValidationErrorResponse(String errorCode, String message, int status, String path, 
                                     LocalDateTime timestamp, Map<String, String> fieldErrors) {
            super(errorCode, message, status, path, timestamp);
            this.fieldErrors = fieldErrors;
        }
        
        public Map<String, String> getFieldErrors() { return fieldErrors; }
    }
}