package com.musicspring.app.music_app.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongReviewResponse {

    private Long songReviewId;

    private Double rating;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private Boolean active;

    private UserProfileResponse user;

    private SongResponse song;

    private Long totalLikes;

    private Long totalDislikes;

    private Long totalLoves;

    private Long totalWows;

    private ReactionResponse userReaction;

    private Long totalComments;
}