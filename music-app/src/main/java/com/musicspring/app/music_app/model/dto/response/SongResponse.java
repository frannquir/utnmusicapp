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

    private String artistSpotifyId;

    private String albumName;

    private String albumSpotifyId;

    private String imageUrl;

    private Integer durationMs;

    private String previewUrl;

    private Boolean explicit;

    private String spotifyLink;

    private LocalDate releaseDate;

}
