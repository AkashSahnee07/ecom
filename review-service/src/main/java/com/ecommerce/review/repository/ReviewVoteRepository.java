package com.ecommerce.review.repository;

import com.ecommerce.review.entity.ReviewVote;
import com.ecommerce.review.entity.ReviewVote.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Review Vote Repository Interface
 * 
 * Provides data access methods for ReviewVote entities to manage
 * helpfulness voting on reviews.
 * 
 * @author E-Commerce Platform Team
 * @version 1.0
 */
@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

    // Basic Queries
    Optional<ReviewVote> findByReviewIdAndUserId(Long reviewId, Long userId);
    
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
    
    List<ReviewVote> findByReviewId(Long reviewId);
    
    List<ReviewVote> findByUserId(Long userId);
    
    List<ReviewVote> findByReviewIdAndVoteType(Long reviewId, VoteType voteType);
    
    List<ReviewVote> findByUserIdAndVoteType(Long userId, VoteType voteType);
    
    // Count Queries
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.reviewId = :reviewId")
    Long countByReviewId(@Param("reviewId") Long reviewId);
    
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.reviewId = :reviewId AND v.voteType = :voteType")
    Long countByReviewIdAndVoteType(@Param("reviewId") Long reviewId, @Param("voteType") VoteType voteType);
    
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.userId = :userId AND v.voteType = :voteType")
    Long countByUserIdAndVoteType(@Param("userId") Long userId, @Param("voteType") VoteType voteType);
    
    // Statistics Queries
    @Query("SELECT v.voteType, COUNT(v) FROM ReviewVote v WHERE v.reviewId = :reviewId GROUP BY v.voteType")
    List<Object[]> getVoteDistribution(@Param("reviewId") Long reviewId);
    
    @Query("SELECT v.reviewId, v.voteType, COUNT(v) FROM ReviewVote v WHERE v.reviewId IN :reviewIds GROUP BY v.reviewId, v.voteType")
    List<Object[]> getVoteDistributionForReviews(@Param("reviewIds") List<Long> reviewIds);
    
    // Date Range Queries
    List<ReviewVote> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<ReviewVote> findByReviewIdAndCreatedDateBetween(Long reviewId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<ReviewVote> findByUserIdAndCreatedDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.reviewId = :reviewId AND v.createdDate BETWEEN :startDate AND :endDate")
    Long countByReviewIdAndDateRange(@Param("reviewId") Long reviewId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // IP Address Queries (for spam detection)
    List<ReviewVote> findByIpAddress(String ipAddress);
    
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.ipAddress = :ipAddress AND v.createdDate >= :since")
    Long countByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.userId = :userId AND v.createdDate >= :since")
    Long countByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    // Analytics Queries
    @Query("SELECT DATE(v.createdDate), COUNT(v) FROM ReviewVote v WHERE v.reviewId = :reviewId GROUP BY DATE(v.createdDate) ORDER BY DATE(v.createdDate)")
    List<Object[]> getVoteCountByDate(@Param("reviewId") Long reviewId);
    
    @Query("SELECT DATE(v.createdDate), v.voteType, COUNT(v) FROM ReviewVote v WHERE v.reviewId = :reviewId GROUP BY DATE(v.createdDate), v.voteType ORDER BY DATE(v.createdDate)")
    List<Object[]> getVoteCountByDateAndType(@Param("reviewId") Long reviewId);
    
    @Query("SELECT v.reviewId, COUNT(v) FROM ReviewVote v GROUP BY v.reviewId ORDER BY COUNT(v) DESC")
    List<Object[]> getMostVotedReviews();
    
    @Query("SELECT v.userId, COUNT(v) FROM ReviewVote v GROUP BY v.userId ORDER BY COUNT(v) DESC")
    List<Object[]> getMostActiveVoters();
    
    // Cleanup Queries
    @Modifying
    @Query("DELETE FROM ReviewVote v WHERE v.createdDate < :cutoffDate")
    int deleteOldVotes(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM ReviewVote v WHERE v.reviewId = :reviewId")
    int deleteByReviewId(@Param("reviewId") Long reviewId);
    
    @Modifying
    @Query("DELETE FROM ReviewVote v WHERE v.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
    
    // Batch Operations
    @Query("SELECT v FROM ReviewVote v WHERE v.reviewId IN :reviewIds")
    List<ReviewVote> findByReviewIds(@Param("reviewIds") List<Long> reviewIds);
    
    @Modifying
    @Query("DELETE FROM ReviewVote v WHERE v.reviewId IN :reviewIds")
    int deleteByReviewIds(@Param("reviewIds") List<Long> reviewIds);
    
    // User Activity Queries
    @Query("SELECT v.reviewId FROM ReviewVote v WHERE v.userId = :userId ORDER BY v.createdDate DESC")
    List<Long> findReviewIdsVotedByUser(@Param("userId") Long userId);
    
    @Query("SELECT v.reviewId, v.voteType FROM ReviewVote v WHERE v.userId = :userId AND v.reviewId IN :reviewIds")
    List<Object[]> findUserVotesForReviews(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
    
    // Fraud Detection Queries
    @Query("SELECT v.userId, COUNT(v) FROM ReviewVote v WHERE v.createdDate >= :since GROUP BY v.userId HAVING COUNT(v) > :threshold ORDER BY COUNT(v) DESC")
    List<Object[]> findSuspiciousVotingActivity(@Param("since") LocalDateTime since, @Param("threshold") Long threshold);
    
    @Query("SELECT v.ipAddress, COUNT(DISTINCT v.userId) FROM ReviewVote v WHERE v.createdDate >= :since GROUP BY v.ipAddress HAVING COUNT(DISTINCT v.userId) > :threshold")
    List<Object[]> findSuspiciousIpActivity(@Param("since") LocalDateTime since, @Param("threshold") Long threshold);
    
    @Query("SELECT v.userId, COUNT(DISTINCT v.reviewId) FROM ReviewVote v WHERE v.createdDate >= :since GROUP BY v.userId HAVING COUNT(DISTINCT v.reviewId) > :threshold")
    List<Object[]> findUsersWithExcessiveVoting(@Param("since") LocalDateTime since, @Param("threshold") Long threshold);
}
