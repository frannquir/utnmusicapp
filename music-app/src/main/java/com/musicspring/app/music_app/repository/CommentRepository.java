package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long>{

    @Query("SELECT c FROM CommentEntity c WHERE c.active = TRUE ")
    Page<CommentEntity> findAll(Pageable pageable);

    @Query("SELECT c FROM CommentEntity c WHERE c.commentId = :commentId AND c.active = true")
    Optional<CommentEntity> findById(@Param("commentId") Long commentId);

    @Query("SELECT c FROM CommentEntity c WHERE c.reviewEntity.reviewId = :reviewId AND c.active = true")
    Page<CommentEntity> findByReviewEntity_ReviewId(@Param("reviewId") Long reviewId, Pageable pageable);

    @Query("SELECT c FROM CommentEntity c WHERE c.user.userId = :userId AND c.active = true")
    Page<CommentEntity> findByUser_UserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM CommentEntity c WHERE c.user.userId = :userId AND c.active = false")
    Page<CommentEntity> findByUser_UserIdAndActiveFalse(@Param("userId") Long userId, Pageable pageable);
}
