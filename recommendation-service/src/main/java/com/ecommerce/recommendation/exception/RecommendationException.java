package com.ecommerce.recommendation.exception;

/**
 * Base exception class for recommendation service
 */
public class RecommendationException extends RuntimeException {
    
    private final String errorCode;
    
    public RecommendationException(String message) {
        super(message);
        this.errorCode = "RECOMMENDATION_ERROR";
    }
    
    public RecommendationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public RecommendationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RECOMMENDATION_ERROR";
    }
    
    public RecommendationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}