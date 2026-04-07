package com.ecommerce.review.service;

import com.ecommerce.review.entity.ReviewSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analyzing sentiment of review content.
 * This is a basic implementation using keyword-based analysis.
 * In production, you might want to integrate with ML services like AWS Comprehend, Google Cloud Natural Language, or Azure Text Analytics.
 */
@Service
public class SentimentAnalysisService {
    
    private static final Logger log = LoggerFactory.getLogger(SentimentAnalysisService.class);

    // Positive sentiment keywords
    private static final List<String> POSITIVE_KEYWORDS = Arrays.asList(
        "excellent", "amazing", "fantastic", "wonderful", "great", "awesome", "perfect",
        "outstanding", "brilliant", "superb", "magnificent", "exceptional", "marvelous",
        "love", "loved", "adore", "enjoy", "enjoyed", "pleased", "satisfied", "happy",
        "delighted", "thrilled", "impressed", "recommend", "recommended", "best",
        "good", "nice", "fine", "solid", "quality", "beautiful", "gorgeous", "stunning",
        "fast", "quick", "efficient", "reliable", "durable", "comfortable", "convenient",
        "helpful", "useful", "valuable", "worth", "affordable", "reasonable", "fair"
    );
    
    // Negative sentiment keywords
    private static final List<String> NEGATIVE_KEYWORDS = Arrays.asList(
        "terrible", "awful", "horrible", "disgusting", "worst", "bad", "poor", "cheap",
        "disappointing", "disappointed", "unsatisfied", "unhappy", "angry", "frustrated",
        "hate", "hated", "dislike", "regret", "waste", "useless", "worthless", "broken",
        "defective", "faulty", "damaged", "slow", "delayed", "late", "expensive",
        "overpriced", "unreliable", "uncomfortable", "difficult", "complicated",
        "confusing", "misleading", "fake", "fraud", "scam", "never", "not", "no",
        "problem", "issue", "trouble", "error", "fail", "failed", "failure", "wrong"
    );
    
    // Very positive intensifiers
    private static final List<String> VERY_POSITIVE_KEYWORDS = Arrays.asList(
        "absolutely", "incredibly", "extremely", "phenomenal", "outstanding", "exceptional",
        "mind-blowing", "life-changing", "perfect", "flawless", "beyond expectations",
        "highly recommend", "must buy", "five stars", "10/10", "couldn't be better"
    );
    
    // Very negative intensifiers
    private static final List<String> VERY_NEGATIVE_KEYWORDS = Arrays.asList(
        "completely", "totally", "absolutely terrible", "worst ever", "never again",
        "money wasted", "complete failure", "avoid at all costs", "zero stars",
        "don't buy", "save your money", "biggest mistake", "utterly disappointed"
    );
    
    // Neutral keywords
    private static final List<String> NEUTRAL_KEYWORDS = Arrays.asList(
        "okay", "ok", "average", "normal", "standard", "typical", "regular", "ordinary",
        "decent", "acceptable", "adequate", "sufficient", "fair", "moderate", "medium",
        "expected", "as described", "nothing special", "works", "functional"
    );
    
    // Negation words that can flip sentiment
    private static final List<String> NEGATION_WORDS = Arrays.asList(
        "not", "no", "never", "nothing", "nobody", "nowhere", "neither", "nor",
        "barely", "hardly", "scarcely", "seldom", "rarely", "don't", "doesn't",
        "didn't", "won't", "wouldn't", "shouldn't", "couldn't", "can't", "cannot"
    );
    
    /**
     * Analyze sentiment of the given text.
     */
    public ReviewSentiment analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return ReviewSentiment.UNKNOWN;
        }
        
        double score = getSentimentScore(text);
        return ReviewSentiment.fromScore(score);
    }
    
    /**
     * Get numerical sentiment score (-1.0 to 1.0).
     * -1.0 = Very Negative, 0.0 = Neutral, 1.0 = Very Positive
     */
    public double getSentimentScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        String cleanText = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");
        String[] words = cleanText.split("\\s+");
        
        double totalScore = 0.0;
        int scoredWords = 0;
        boolean negationContext = false;
        
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            
            // Check for negation
            if (NEGATION_WORDS.contains(word)) {
                negationContext = true;
                continue;
            }
            
            double wordScore = getWordSentimentScore(word);
            
            if (wordScore != 0.0) {
                // Apply negation if in negation context
                if (negationContext) {
                    wordScore = -wordScore * 0.8; // Reduce intensity when negated
                    negationContext = false; // Reset negation context
                }
                
                // Check for intensifiers in the next few words
                wordScore = applyIntensifiers(words, i, wordScore);
                
                totalScore += wordScore;
                scoredWords++;
            }
            
            // Reset negation context after a few words
            if (negationContext && i > 0 && (i % 3 == 0)) {
                negationContext = false;
            }
        }
        
        if (scoredWords == 0) {
            return 0.0;
        }
        
        double averageScore = totalScore / scoredWords;
        
        // Apply text-level adjustments
        averageScore = applyTextLevelAdjustments(text, averageScore);
        
        // Normalize to [-1.0, 1.0] range
        return Math.max(-1.0, Math.min(1.0, averageScore));
    }
    
    /**
     * Get sentiment score for a single word.
     */
    private double getWordSentimentScore(String word) {
        if (VERY_POSITIVE_KEYWORDS.contains(word)) {
            return 1.0;
        } else if (POSITIVE_KEYWORDS.contains(word)) {
            return 0.6;
        } else if (NEUTRAL_KEYWORDS.contains(word)) {
            return 0.0;
        } else if (NEGATIVE_KEYWORDS.contains(word)) {
            return -0.6;
        } else if (VERY_NEGATIVE_KEYWORDS.contains(word)) {
            return -1.0;
        }
        return 0.0;
    }
    
    /**
     * Apply intensifiers to modify word sentiment score.
     */
    private double applyIntensifiers(String[] words, int currentIndex, double baseScore) {
        double multiplier = 1.0;
        
        // Check previous words for intensifiers
        for (int i = Math.max(0, currentIndex - 2); i < currentIndex; i++) {
            String prevWord = words[i];
            if (isIntensifier(prevWord)) {
                multiplier *= getIntensifierMultiplier(prevWord);
            }
        }
        
        // Check next words for intensifiers
        for (int i = currentIndex + 1; i < Math.min(words.length, currentIndex + 3); i++) {
            String nextWord = words[i];
            if (isIntensifier(nextWord)) {
                multiplier *= getIntensifierMultiplier(nextWord);
            }
        }
        
        return baseScore * multiplier;
    }
    
    /**
     * Check if a word is an intensifier.
     */
    private boolean isIntensifier(String word) {
        List<String> intensifiers = Arrays.asList(
            "very", "extremely", "incredibly", "absolutely", "completely", "totally",
            "really", "quite", "pretty", "rather", "somewhat", "fairly", "highly",
            "deeply", "truly", "genuinely", "seriously", "definitely", "certainly"
        );
        return intensifiers.contains(word);
    }
    
    /**
     * Get multiplier for intensifier words.
     */
    private double getIntensifierMultiplier(String intensifier) {
        Map<String, Double> multipliers = Map.of(
            "very", 1.3,
            "extremely", 1.5,
            "incredibly", 1.5,
            "absolutely", 1.4,
            "completely", 1.4,
            "totally", 1.4,
            "really", 1.2,
            "quite", 1.1,
            "pretty", 1.1,
            "rather", 1.1
        );
        return multipliers.getOrDefault(intensifier, 1.2);
    }
    
    /**
     * Apply text-level adjustments based on overall patterns.
     */
    private double applyTextLevelAdjustments(String text, double baseScore) {
        double adjustedScore = baseScore;
        
        // Exclamation marks indicate stronger sentiment
        long exclamationCount = text.chars().filter(ch -> ch == '!').count();
        if (exclamationCount > 0) {
            double exclamationMultiplier = 1.0 + (exclamationCount * 0.1);
            adjustedScore *= Math.min(exclamationMultiplier, 1.5);
        }
        
        // All caps indicates stronger sentiment
        if (text.equals(text.toUpperCase()) && text.length() > 10) {
            adjustedScore *= 1.2;
        }
        
        // Question marks might indicate uncertainty (reduce confidence)
        long questionCount = text.chars().filter(ch -> ch == '?').count();
        if (questionCount > 2) {
            adjustedScore *= 0.9;
        }
        
        // Length adjustment - very short reviews might be less reliable
        if (text.length() < 20) {
            adjustedScore *= 0.8;
        }
        
        return adjustedScore;
    }
    
    /**
     * Get detailed sentiment analysis with confidence score.
     */
    public SentimentAnalysisResult getDetailedSentimentAnalysis(String text) {
        if (text == null || text.trim().isEmpty()) {
            return SentimentAnalysisResult.builder()
                .sentiment(ReviewSentiment.UNKNOWN)
                .score(0.0)
                .confidence(0.0)
                .wordCount(0)
                .build();
        }
        
        double score = getSentimentScore(text);
        ReviewSentiment sentiment = ReviewSentiment.fromScore(score);
        
        String[] words = text.toLowerCase().split("\\s+");
        int wordCount = words.length;
        
        // Calculate confidence based on various factors
        double confidence = calculateConfidence(text, score, wordCount);
        
        return SentimentAnalysisResult.builder()
            .sentiment(sentiment)
            .score(score)
            .confidence(confidence)
            .wordCount(wordCount)
            .build();
    }
    
    /**
     * Calculate confidence score for sentiment analysis.
     */
    private double calculateConfidence(String text, double score, int wordCount) {
        double confidence = 0.5; // Base confidence
        
        // Higher confidence for longer texts
        if (wordCount > 50) {
            confidence += 0.2;
        } else if (wordCount > 20) {
            confidence += 0.1;
        } else if (wordCount < 5) {
            confidence -= 0.2;
        }
        
        // Higher confidence for stronger sentiment scores
        double absScore = Math.abs(score);
        if (absScore > 0.7) {
            confidence += 0.2;
        } else if (absScore > 0.4) {
            confidence += 0.1;
        } else if (absScore < 0.1) {
            confidence -= 0.1;
        }
        
        // Count sentiment-bearing words
        String[] words = text.toLowerCase().split("\\s+");
        long sentimentWords = Arrays.stream(words)
            .filter(word -> getWordSentimentScore(word) != 0.0)
            .count();
        
        double sentimentRatio = (double) sentimentWords / wordCount;
        if (sentimentRatio > 0.3) {
            confidence += 0.1;
        } else if (sentimentRatio < 0.1) {
            confidence -= 0.1;
        }
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    /**
     * Data class for detailed sentiment analysis results.
     */
    public static class SentimentAnalysisResult {
        private ReviewSentiment sentiment;
        private double score;
        private double confidence;
        private int wordCount;
        
        public SentimentAnalysisResult() {}
        
        public SentimentAnalysisResult(ReviewSentiment sentiment, double score, double confidence, int wordCount) {
            this.sentiment = sentiment;
            this.score = score;
            this.confidence = confidence;
            this.wordCount = wordCount;
        }
        
        public static SentimentAnalysisResultBuilder builder() {
            return new SentimentAnalysisResultBuilder();
        }
        
        public ReviewSentiment getSentiment() { return sentiment; }
        public void setSentiment(ReviewSentiment sentiment) { this.sentiment = sentiment; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public int getWordCount() { return wordCount; }
        public void setWordCount(int wordCount) { this.wordCount = wordCount; }
        
        public static class SentimentAnalysisResultBuilder {
            private ReviewSentiment sentiment;
            private double score;
            private double confidence;
            private int wordCount;
            
            public SentimentAnalysisResultBuilder sentiment(ReviewSentiment sentiment) {
                this.sentiment = sentiment;
                return this;
            }
            
            public SentimentAnalysisResultBuilder score(double score) {
                this.score = score;
                return this;
            }
            
            public SentimentAnalysisResultBuilder confidence(double confidence) {
                this.confidence = confidence;
                return this;
            }
            
            public SentimentAnalysisResultBuilder wordCount(int wordCount) {
                this.wordCount = wordCount;
                return this;
            }
            
            public SentimentAnalysisResult build() {
                return new SentimentAnalysisResult(sentiment, score, confidence, wordCount);
            }
        }
    }
    
    /**
     * Batch analyze sentiment for multiple texts.
     */
    public Map<String, ReviewSentiment> batchAnalyzeSentiment(List<String> texts) {
        return texts.stream()
            .collect(Collectors.toMap(
                text -> text,
                this::analyzeSentiment
            ));
    }
    
    /**
     * Get sentiment distribution for a list of texts.
     */
    public Map<ReviewSentiment, Long> getSentimentDistribution(List<String> texts) {
        return texts.stream()
            .map(this::analyzeSentiment)
            .collect(Collectors.groupingBy(
                sentiment -> sentiment,
                Collectors.counting()
            ));
    }
}
