package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistWithAlbumsResponse;
import com.musicspring.app.music_app.model.entity.ArtistEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.model_objects.specification.Artist;

import java.util.List;

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

    public ArtistEntity withAlbumstoEntity(ArtistWithAlbumsResponse response) {
        return ArtistEntity.builder()
                .spotifyId(response.getSpotifyId())
                .name(response.getName())
                .followers(response.getFollowers())
                .imageUrl(response.getImageUrl())
                .spotifyLink(response.getSpotifyLink())
                .build();
    }

    public ArtistWithAlbumsResponse spotifyArtistToArtistWithAlbumesResponse(Artist artist, List<AlbumResponse> albumRes){
        return ArtistWithAlbumsResponse.builder()
                .artistId(null)
                .name(artist.getName())
                .followers(artist.getFollowers() != null ? artist.getFollowers().getTotal() : 0)
                .imageUrl(artist.getImages() != null && artist.getImages().length > 0 ? artist.getImages()[0].getUrl() : null)
                .spotifyId(artist.getId())
                .spotifyLink(artist.getExternalUrls() != null ? artist.getExternalUrls().get("spotify") : null)
                .albums(albumRes)
                .build();
    }

}

