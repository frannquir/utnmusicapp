package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.CommentRequest;
import com.musicspring.app.music_app.model.dto.response.CommentResponse;
import com.musicspring.app.music_app.model.dto.response.ReactionResponse;
import com.musicspring.app.music_app.model.entity.CommentEntity;
import com.musicspring.app.music_app.model.entity.ReviewEntity;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.model.enums.CommentType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(CommentEntity comment){
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .reviewId(comment.getReviewEntity().getReviewId())
                .userId(comment.getUser().getUserId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .commentType(comment.getCommentType())
                .username(comment.getUser().getUsername())
                .build();
    }

    public CommentResponse toResponse(
            CommentEntity comment,
            Long totalLikes,
            Long totalDislikes,
            Long totalLoves,
            Long totalWows,
            ReactionResponse userReaction
    ) {
        return CommentResponse.builder()

                .commentId(comment.getCommentId())
                .reviewId(comment.getReviewEntity().getReviewId())
                .userId(comment.getUser().getUserId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .commentType(comment.getCommentType())
                .username(comment.getUser().getUsername())
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .totalLoves(totalLoves)
                .totalWows(totalWows)
                .userReaction(userReaction)
                .build();
    }

    public Page<CommentResponse> toResponsePage(Page<CommentEntity> commentPage){
        return commentPage.map(this::toResponse);
    }

    public CommentEntity toEntity(CommentRequest commentRequest,
                                  UserEntity userEntity,
                                  ReviewEntity reviewEntity,
                                  CommentType commentType) {

        return CommentEntity.builder()
                .text(commentRequest.getText())
                .user(userEntity)
                .reviewEntity(reviewEntity)
                .commentType(commentType)
                .build();
    }
}
