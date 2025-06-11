package com.musicspring.app.music_app.model.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumResponse {

    private Long albumId;

    private String spotifyId;

    private String title;

    private String artistName;

    private String imageUrl;

    private String spotifyLink;

    private LocalDate releaseDate;

}
