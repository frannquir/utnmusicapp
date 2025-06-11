package com.musicspring.app.music_app.model.dto.response;

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

    private LocalDateTime date;

    private Boolean active;

    private UserResponse user;

    private SongResponse song;
}