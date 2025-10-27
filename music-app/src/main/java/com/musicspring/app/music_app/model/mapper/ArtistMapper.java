package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.entity.ArtistEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ArtistMapper {

    public ArtistResponse toResponse(ArtistEntity entity) {
        return ArtistResponse.builder()
                .artistId(entity.getArtistId())
                .spotifyId(entity.getSpotifyId())
                .name(entity.getName())
                .followers(entity.getFollowers())
                .imageUrl(entity.getImageUrl())
                .spotifyLink(entity.getSpotifyLink())
                .build();
    }

    public Page<ArtistResponse> toResponsePage(Page<ArtistEntity> artistEntityPage){
        return artistEntityPage.map(this::toResponse);
    }

    public ArtistEntity toEntity(ArtistResponse response) {
        return ArtistEntity.builder()
                .spotifyId(response.getSpotifyId())
                .name(response.getName())
                .followers(response.getFollowers())
                .imageUrl(response.getImageUrl())
                .spotifyLink(response.getSpotifyLink())
                .build();
    }
}

