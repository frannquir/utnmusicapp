package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.AlbumRequest;
import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.entity.AlbumEntity;
import com.musicspring.app.music_app.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import java.util.List;

@Component
public class AlbumMapper {


    public AlbumResponse toResponse(AlbumEntity album){
        return AlbumResponse.builder()
                .albumId(album.getAlbumId())
                .spotifyId(album.getSpotifyId())
                .title(album.getTitle())
                .artistName(album.getArtist().getName())
                .imageUrl(album.getImageUrl())
                .spotifyLink(album.getSpotifyLink())
                .releaseDate(album.getReleaseDate())
                .build();
    }

    public Page<AlbumEntity> toEntityPage(List<AlbumEntity> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<AlbumEntity> sublist = list.subList(start, end);
        return new PageImpl<>(sublist, pageable, list.size());
    }


    public Page<AlbumResponse> toResponsePage (Page<AlbumEntity> albumEntityPage){
        return albumEntityPage.map(this::toResponse);
    }

    public AlbumEntity requestToEntity (AlbumRequest albumRequest){
        return AlbumEntity.builder()
                .spotifyId(albumRequest.getSpotifyId())
                .title(albumRequest.getTitle())
                .imageUrl(albumRequest.getImageUrl())
                .spotifyLink(albumRequest.getSpotifyLink())
                .releaseDate(albumRequest.getReleaseDate())
                .build();
    }
}

