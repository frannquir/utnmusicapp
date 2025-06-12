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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<ReactionEntity, Long> {

    @Query("SELECT r FROM ReactionEntity r " +
            "WHERE (:reaction IS NULL OR r.reactionType = :reaction) " +
            "AND (:reacted IS NULL OR r.reactedType = :reacted)")
    Page<ReactionEntity> findByReactionTypeAndReactedType(@Param("reaction") ReactionType reactionType,
                                                          @Param("reacted") ReactedType reactedType,
                                                          Pageable pageable);

    Page<ReactionEntity> findByUser_UserId(Long userId, Pageable pageable);

    Page<ReactionEntity> findByComment_CommentId(Long commentId, Pageable pageable);

    Page<ReactionEntity> findByReview_ReviewId(Long reviewId, Pageable pageable);

    Optional<ReactionEntity> findByUserAndReview(UserEntity user, ReviewEntity review);
    Optional<ReactionEntity> findByUserAndComment(UserEntity user, CommentEntity comment);
}
