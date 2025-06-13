package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.ReactionRequest;
import com.musicspring.app.music_app.model.dto.response.ReactionResponse;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.enums.ReactedType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReactionMapper {

    public ReactionResponse toResponse(ReactionEntity reaction){
        return ReactionResponse.builder()
                .reactionId(reaction.getId())
                .userId(reaction.getUser().getUserId())
                .username(reaction.getUser().getUsername())
                .reactionType(reaction.getReactionType())
                .reactedType(reaction.getReactedType())
                .reactedId(reaction.getReview() != null
                        ? reaction.getReview().getReviewId()
                        : reaction.getComment().getCommentId())
                .build();
    }

    public Page<ReactionResponse> toResponsePage(Page<ReactionEntity> reactionPage){
        return reactionPage.map(this::toResponse);
    }

    public List<ReactionResponse> toResponseList(List<ReactionEntity> reactionList) {
        return reactionList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReactionEntity toEntity(ReactionRequest request, UserEntity user, CommentEntity comment){
        return ReactionEntity.builder()
                .reactionType(request.getReactionType())
                .reactedType(ReactedType.COMMENT) // fijamos el tipo explícitamente
                .user(user)
                .comment(comment)
                .build();
    }

    public ReactionEntity toEntity(ReactionRequest request, UserEntity user, ReviewEntity review){
        return ReactionEntity.builder()
                .reactionType(request.getReactionType())
                .reactedType(ReactedType.REVIEW) // fijamos el tipo explícitamente
                .user(user)
                .review(review)
                .build();
    }
}
