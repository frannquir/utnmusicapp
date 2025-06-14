package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.SongReviewRequest;
import com.musicspring.app.music_app.model.dto.request.SongRequest;
import com.musicspring.app.music_app.model.dto.response.SongResponse;
import com.musicspring.app.music_app.model.entity.SongEntity;
import com.musicspring.app.music_app.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
public class SongMapper {

    private final AlbumService albumService;

    @Autowired
    public SongMapper(AlbumService albumService) {
        this.albumService = albumService;
    }


    public SongResponse toResponse(SongEntity song) {
        return SongResponse.builder()
                .songId(song.getSongId())
                .spotifyId(song.getSpotifyId())
                .name(song.getName())
                .artistName(song.getAlbum().getArtist().getName())
                .albumName(song.getAlbum().getTitle())
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

    public SongEntity toEntity(SongRequest song){
        return SongEntity.builder()
                .spotifyId(song.getSpotifyId())
                .name(song.getName())
                .album(albumService.findByIdEntity(song.getAlbumId()))
                .imageUrl(song.getImageUrl())
                .durationMs(song.getDurationMs())
                .previewUrl(song.getPreviewUrl())
                .spotifyLink(song.getSpotifyLink())
                .releaseDate(song.getReleaseDate())
                .build();
    }

}
