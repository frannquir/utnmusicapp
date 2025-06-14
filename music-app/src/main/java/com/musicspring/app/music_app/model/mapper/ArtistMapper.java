package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.ArtistRequest;
import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistWithAlbumsResponse;
import com.musicspring.app.music_app.model.entity.ArtistEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArtistMapper {

    public ArtistResponse toResponse(ArtistEntity entity) {
        return ArtistResponse.builder()
                .artistId(entity.getArtistId())
                .spotifyId(entity.getSpotifyId())
                .name(entity.getName())
                .followers(entity.getFollowers())
                .imageUrl(entity.getImageUrl())
                .build();
    }

    public Page<ArtistResponse> toResponsePage(Page<ArtistEntity> artistEntityPage){
        return artistEntityPage.map(this::toResponse);
    }

    public ArtistEntity toEntity(ArtistRequest request) {
        return ArtistEntity.builder()
                .spotifyId(request.getSpotifyId())
                .name(request.getName())
                .followers(request.getFollowers())
                .imageUrl(request.getImageUrl())
                .build();
    }
}

