package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.entity.AlbumEntity;
import com.musicspring.app.music_app.model.mapper.AlbumMapper;
import com.musicspring.app.music_app.repository.AlbumRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService  {

    private final AlbumRepository albumRepository;
    private final AlbumMapper albumMapper;

    @Autowired
    public AlbumService(AlbumRepository albumRepository, AlbumMapper albumMapper) {
        this.albumRepository = albumRepository;
        this.albumMapper = albumMapper;
    }


    public Page<AlbumResponse> findAll(Pageable pageable) {
        return albumMapper.toResponsePage(albumRepository.findAll(pageable));
    }

    public AlbumResponse findById(Long id) {
        return albumMapper.toResponse(albumRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Album with ID " + id + " not found.")));
    }

    public AlbumResponse findBySpotifyId(String spotifyId){
        return albumMapper.toResponse(albumRepository.findBySpotifyId(spotifyId).orElseThrow(()
                -> new EntityNotFoundException("Album with Spotify ID " + spotifyId + " not found.")));
    }

    public Page<AlbumResponse> searchAlbums(String query, Pageable pageable){
        List<AlbumEntity> albumEntityList =  albumRepository.findByTitleContainingIgnoreCase
                (query, pageable);
        Page<AlbumEntity> albumEntityPage = albumMapper.toEntityPage(albumEntityList,pageable);
        return albumMapper.toResponsePage(albumEntityPage);
    }

}
