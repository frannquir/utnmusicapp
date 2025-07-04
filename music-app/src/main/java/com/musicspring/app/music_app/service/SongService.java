package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.SongRequest;
import com.musicspring.app.music_app.model.dto.response.SongResponse;
import com.musicspring.app.music_app.model.entity.SongEntity;
import com.musicspring.app.music_app.model.mapper.SongMapper;
import com.musicspring.app.music_app.repository.SongRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class SongService  {

    private final SongRepository songRepository;
    private final SongMapper songMapper;

    public SongService(SongRepository songRepository, SongMapper songMapper) {
        this.songRepository = songRepository;
        this.songMapper = songMapper;
    }

    public Page<SongResponse> findAll(Pageable pageable) {
        return songMapper.toResponsePage(songRepository.findAll(pageable));
    }

    public SongResponse findById(Long id) {
        return songMapper.toResponse(songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song with ID: " + id + " not found.")));
    }

    public SongResponse findBySpotifyId(String spotifyId) {
        return songMapper.toResponse(songRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new EntityNotFoundException("Song with Spotify ID: " + spotifyId + " not found.")));
    }


    public Page<SongResponse> searchSongs(String query, Pageable pageable) {
        Page<SongEntity> songPage = songRepository.findByNameContainingIgnoreCase(
                query, query, pageable
        );

        return songMapper.toResponsePage(songPage);
    }
}
