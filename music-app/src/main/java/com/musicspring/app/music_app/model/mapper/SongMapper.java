package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.response.SongResponse;
import com.musicspring.app.music_app.model.entity.SongEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {


    public SongResponse toResponse(SongEntity song) {
        return SongResponse.builder()
                .songId(song.getSongId())
                .spotifyId(song.getSpotifyId())
                .name(song.getName())
                .artistName(song.getAlbum().getArtist().getName())
                .artistSpotifyId(song.getAlbum().getArtist().getSpotifyId())
                .albumName(song.getAlbum().getTitle())
                .albumSpotifyId(song.getAlbum().getSpotifyId())
                .imageUrl(song.getImageUrl())
                .durationMs(song.getDurationMs())
                .previewUrl(song.getPreviewUrl())
                .spotifyLink(song.getSpotifyLink())
                .releaseDate(song.getReleaseDate())
                .build();
    }

    public Page<SongResponse> toResponsePage(Page<SongEntity> songEntityPage){
        return songEntityPage.map(this::toResponse);
    }

    public SongEntity toEntity(SongResponse song){
        return SongEntity.builder()
                .spotifyId(song.getSpotifyId())
                .name(song.getName())
                .imageUrl(song.getImageUrl())
                .durationMs(song.getDurationMs())
                .previewUrl(song.getPreviewUrl())
                .spotifyLink(song.getSpotifyLink())
                .releaseDate(song.getReleaseDate())
                .build();
    }

}
