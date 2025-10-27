package com.musicspring.app.music_app.spotify.model;

import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.SongResponse;
import lombok.*;
import org.springframework.data.domain.Page;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedSearchResponse {
    private Page<SongResponse> songs;
    private Page<ArtistResponse> artists;
    private Page<AlbumResponse> albums;
    private String query;

}
