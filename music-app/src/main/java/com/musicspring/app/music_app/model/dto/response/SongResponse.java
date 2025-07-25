package com.musicspring.app.music_app.model.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {

    private Long songId;

    private String spotifyId;

    private String name;

    private String artistName;

    private String albumName;

    private String imageUrl;

    private Integer durationMs;

    private String previewUrl;

    private String spotifyLink;

    private LocalDate releaseDate;

}
