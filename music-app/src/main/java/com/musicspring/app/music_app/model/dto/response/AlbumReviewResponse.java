package com.musicspring.app.music_app.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumReviewResponse {

    private Long albumReviewId;

    private Double rating;

    private String description;

    private LocalDateTime date;

    private Boolean active;

    private UserResponse user;

    private AlbumResponse album;


}