package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.CommentEntity;
import com.musicspring.app.music_app.model.entity.ReactionEntity;
import com.musicspring.app.music_app.model.entity.ReviewEntity;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.model.enums.ReactedType;
import com.musicspring.app.music_app.model.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<ReactionEntity, Long> {

    @Query("SELECT r FROM ReactionEntity r " +
            "WHERE (:reaction IS NULL OR r.reactionType = :reaction) " +
            "AND (:reacted IS NULL OR r.reactedType = :reacted)")
    Page<ReactionEntity> findByReactionTypeAndReactedType(@Param("reaction") ReactionType reactionType,
                                                          @Param("reacted") ReactedType reactedType,
                                                          Pageable pageable);

    @Query("SELECT r FROM ReactionEntity r " +
            "WHERE r.user.userId = :userId AND " +
            "((r.review IS NOT NULL AND r.review.active = true) OR " +
            "(r.comment IS NOT NULL AND r.comment.active = true))")
    Page<ReactionEntity> findByUser_UserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM ReactionEntity r WHERE r.comment.commentId = :commentId AND r.comment.active = TRUE")
    Page<ReactionEntity> findByComment_CommentId(@Param("commentId") Long commentId, Pageable pageable);

    @Query("SELECT r FROM ReactionEntity r WHERE r.review.reviewId = :reviewId AND r.review.active = TRUE")
    Page<ReactionEntity> findByReview_ReviewId(@Param("reviewId") Long reviewId, Pageable pageable);


    Optional<ReactionEntity> findByUserAndReview(UserEntity user, ReviewEntity review);
    Optional<ReactionEntity> findByUserAndComment(UserEntity user, CommentEntity comment);

    @Modifying
    @Query("DELETE FROM ReactionEntity r WHERE r.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ReactionEntity r WHERE r.review.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("DELETE FROM ReactionEntity r WHERE r.comment.commentId IN (SELECT c.commentId FROM CommentEntity c WHERE c.reviewEntity.reviewId = :reviewId)")
    void deleteReactionsOnReviewComments(@Param("reviewId") Long reviewId);

    @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.user.userId = :userId AND r.reactionType = :type")
    Long countReactionsByUserAndType(@Param("userId") Long userId, @Param("type") ReactionType type);

    @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.review.user.userId = :userId AND r.reactionType = :type")
    Long countReactionsReceivedOnReviews(@Param("userId") Long userId, @Param("type") ReactionType type);

    @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.comment.user.userId = :userId AND r.reactionType = :type")
    Long countReactionsReceivedOnComments(@Param("userId") Long userId, @Param("type") ReactionType type);

    @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.user.userId = :userId")
    Long countByUserUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.user.userId = :userId AND r.createdAt >= :startOfMonth")
    Long countReactionsThisMonth(@Param("userId") Long userId, @Param("startOfMonth") LocalDateTime startOfMonth);
}
