package com.ecommerce.review.entity;

/**
 * Review Sentiment Enumeration
 * 
 * Defines the sentiment analysis results for reviews.
 * Used for analytics and filtering purposes.
 * 
 * @author E-Commerce Platform Team
 * @version 1.0
 */
public enum ReviewSentiment {
    
    /**
     * Very positive sentiment (score >= 0.6)
     */
    VERY_POSITIVE("Very Positive", 0.6, 1.0, "😍"),
    
    /**
     * Positive sentiment (score >= 0.2 and < 0.6)
     */
    POSITIVE("Positive", 0.2, 0.6, "😊"),
    
    /**
     * Neutral sentiment (score >= -0.2 and < 0.2)
     */
    NEUTRAL("Neutral", -0.2, 0.2, "😐"),
    
    /**
     * Negative sentiment (score >= -0.6 and < -0.2)
     */
    NEGATIVE("Negative", -0.6, -0.2, "😞"),
    
    /**
     * Very negative sentiment (score < -0.6)
     */
    VERY_NEGATIVE("Very Negative", -1.0, -0.6, "😡"),
    
    /**
     * Mixed sentiment - contains both positive and negative elements
     */
    MIXED("Mixed", -1.0, 1.0, "🤔"),
    
    /**
     * Unknown sentiment - analysis could not determine sentiment
     */
    UNKNOWN("Unknown", -1.0, 1.0, "❓");
    
    private final String displayName;
    private final double minScore;
    private final double maxScore;
    private final String emoji;
    
    ReviewSentiment(String displayName, double minScore, double maxScore, String emoji) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.emoji = emoji;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getMinScore() {
        return minScore;
    }
    
    public double getMaxScore() {
        return maxScore;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    /**
     * Determine sentiment based on score
     * @param score sentiment score between -1.0 and 1.0
     * @return corresponding ReviewSentiment
     */
    public static ReviewSentiment fromScore(double score) {
        if (score >= 0.6) {
            return VERY_POSITIVE;
        } else if (score >= 0.2) {
            return POSITIVE;
        } else if (score >= -0.2) {
            return NEUTRAL;
        } else if (score >= -0.6) {
            return NEGATIVE;
        } else {
            return VERY_NEGATIVE;
        }
    }
    
    /**
     * Check if sentiment is positive (positive or very positive)
     * @return true if sentiment is positive
     */
    public boolean isPositive() {
        return this == POSITIVE || this == VERY_POSITIVE;
    }
    
    /**
     * Check if sentiment is negative (negative or very negative)
     * @return true if sentiment is negative
     */
    public boolean isNegative() {
        return this == NEGATIVE || this == VERY_NEGATIVE;
    }
    
    /**
     * Check if sentiment is neutral
     * @return true if sentiment is neutral
     */
    public boolean isNeutral() {
        return this == NEUTRAL;
    }
    
    /**
     * Get sentiment weight for calculations
     * @return weight value for sentiment
     */
    public double getWeight() {
        switch (this) {
            case VERY_POSITIVE:
                return 2.0;
            case POSITIVE:
                return 1.0;
            case NEUTRAL:
                return 0.0;
            case NEGATIVE:
                return -1.0;
            case VERY_NEGATIVE:
                return -2.0;
            case MIXED:
                return 0.0;
            default:
                return 0.0;
        }
    }
    
    /**
     * Get color code for UI display
     * @return hex color code
     */
    public String getColorCode() {
        switch (this) {
            case VERY_POSITIVE:
                return "#28a745"; // Green
            case POSITIVE:
                return "#6f9654"; // Light Green
            case NEUTRAL:
                return "#6c757d"; // Gray
            case NEGATIVE:
                return "#fd7e14"; // Orange
            case VERY_NEGATIVE:
                return "#dc3545"; // Red
            case MIXED:
                return "#17a2b8"; // Teal
            default:
                return "#6c757d"; // Gray
        }
    }
    
    /**
     * Get all positive sentiments
     * @return array of positive sentiments
     */
    public static ReviewSentiment[] getPositiveSentiments() {
        return new ReviewSentiment[]{VERY_POSITIVE, POSITIVE};
    }
    
    /**
     * Get all negative sentiments
     * @return array of negative sentiments
     */
    public static ReviewSentiment[] getNegativeSentiments() {
        return new ReviewSentiment[]{NEGATIVE, VERY_NEGATIVE};
    }
    
    /**
     * Get display string with emoji
     * @return formatted display string
     */
    public String getDisplayWithEmoji() {
        return emoji + " " + displayName;
    }
}
