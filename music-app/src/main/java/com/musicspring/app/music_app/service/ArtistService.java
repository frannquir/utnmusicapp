package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistWithAlbumsResponse;
import com.musicspring.app.music_app.model.entity.ArtistEntity;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
import com.musicspring.app.music_app.repository.ArtistRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<ArtistResponse> findAll(Pageable pageable) {
        return artistMapper.toResponsePage(artistRepository.findAll(pageable));
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

    public ArtistEntity findById(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist with ID " + id + " not found."));
    }

}