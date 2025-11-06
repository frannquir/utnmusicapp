package com.musicspring.app.music_app.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.musicspring.app.music_app.model.enums.CommentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CommentResponse {

    private Long commentId;

    private Long reviewId;

    private Long userId;

    private String text;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private CommentType commentType;

    private String username;

    private Long totalLikes;

    private Long totalDislikes;

    private Long totalLoves;

    private Long totalWows;

    private ReactionResponse userReaction;

}
