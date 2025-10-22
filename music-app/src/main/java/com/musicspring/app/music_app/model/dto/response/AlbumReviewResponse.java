package com.musicspring.app.music_app.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private Boolean active;

    private UserProfileResponse user;

    private AlbumResponse album;


}