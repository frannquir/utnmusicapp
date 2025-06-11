package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.ArtistRequest;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistWithSongsResponse;
import com.musicspring.app.music_app.model.entity.AlbumEntity;
import com.musicspring.app.music_app.model.entity.ArtistEntity;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
import com.musicspring.app.music_app.repository.ArtistRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArtistService {
    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;

    @Autowired
    public ArtistService(ArtistRepository artistRepository,
                         ArtistMapper artistMapper) {
        this.artistRepository = artistRepository;
        this.artistMapper = artistMapper;
    }

    public ArtistWithSongsResponse getArtistWithSongs(Long artistId) {
        ArtistEntity artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist with ID " + artistId + " not found."));
        return artistMapper.toArtistWithSongsResponse(artist);
    }

    public void deleteById(Long id) {
        ArtistEntity artist = artistRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Artist with ID " + id + " not found."));
        artistRepository.deleteById(id);
    }

    public Page<ArtistResponse> findAll(Pageable pageable) {
        return artistMapper.toResponsePage(artistRepository.findAll(pageable));
    }

    private ArtistResponse findById(Long id) {
        return artistMapper.toResponse(artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist with id " + id + " not found")));
    }

    public ArtistResponse getArtistResponseById(Long id) {
        return artistRepository.findById(id)
                .map(artistMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Artist with id " + id + " not found"));
    }

    public Page<ArtistResponse> getAllArtists(Pageable pageable) {
        return artistRepository.findAll(pageable)
                .map(artistMapper::toResponse);
    }

    public Page<ArtistResponse> searchArtists(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query must not be null or empty");
        }

        Page<ArtistEntity> artistPage = artistRepository
                .findByNameContainingIgnoreCase(query.trim(), pageable);

        return artistPage.map(artistMapper::toResponse);
    }

}