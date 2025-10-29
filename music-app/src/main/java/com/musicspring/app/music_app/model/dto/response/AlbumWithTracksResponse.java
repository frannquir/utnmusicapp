package com.musicspring.app.music_app.model.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumWithTracksResponse {
    private Long albumId; // Keep null if not from DB
    private String spotifyId;
    private String title;
    private String artistName;
    private String artistSpotifyId;
    private String imageUrl;
    private String spotifyLink;
    private LocalDate releaseDate;
    private List<SongResponse> songs;
}
