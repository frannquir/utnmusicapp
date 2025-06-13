package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.*;
import com.musicspring.app.music_app.model.dto.response.AlbumReviewResponse;
import com.musicspring.app.music_app.model.dto.response.SongReviewResponse;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.mapper.AlbumMapper;
import com.musicspring.app.music_app.model.mapper.AlbumReviewMapper;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
import com.musicspring.app.music_app.repository.AlbumRepository;
import com.musicspring.app.music_app.repository.AlbumReviewRepository;
import com.musicspring.app.music_app.repository.ArtistRepository;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.service.AuthService;
import com.musicspring.app.music_app.spotify.service.SpotifyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AlbumReviewService {

    private final AlbumReviewRepository albumReviewRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final AlbumReviewMapper albumReviewMapper;
    private final SpotifyService spotifyService;
    private final AlbumMapper albumMapper;
    private final ArtistMapper artistMapper;
    private final ArtistRepository artistRepository;

    @Autowired
    public AlbumReviewService(AlbumReviewRepository albumReviewRepository, AlbumRepository albumRepository, UserRepository userRepository, AlbumReviewMapper albumReviewMapper, SpotifyService spotifyService, AlbumMapper albumMapper, ArtistMapper artistMapper, ArtistRepository artistRepository) {
        this.albumReviewRepository = albumReviewRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.albumReviewMapper = albumReviewMapper;
        this.spotifyService = spotifyService;
        this.albumMapper = albumMapper;
        this.artistMapper = artistMapper;
        this.artistRepository = artistRepository;
    }


    public Page<AlbumReviewResponse> findAll(Pageable pageable) {
        return albumReviewMapper.toResponsePage(albumReviewRepository.findAll(pageable));
    }

    public AlbumReviewResponse findById(Long id) {
        return albumReviewMapper.toResponse(albumReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album review with ID: " + id + " not found.")));
    }

    public void deleteById(Long id) {
        AlbumReviewEntity albumReviewEntity = albumReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album review with ID: " + id + " not found."));
        albumReviewEntity.setActive(false);
        albumReviewRepository.save(albumReviewEntity);
    }

    public AlbumReviewResponse createAlbumReview(Long albumId, String spotifyId,
                                               AlbumReviewRequest albumReviewRequest) {
        validateIdentifiers(albumId, spotifyId);

        if (albumReviewRequest.getUserId() == null) {
            throw new IllegalArgumentException("UserId is required");
        }

        Long authenticatedUserId = AuthService.extractUserId();
        AuthService.validateUserOwnership(authenticatedUserId, albumReviewRequest.getUserId());

        UserEntity userEntity = userRepository.findById(albumReviewRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + albumReviewRequest.getUserId() + " not found."));

        AlbumEntity albumEntity = findOrCreateAlbumEntity(albumId, spotifyId);

        AlbumReviewEntity albumReviewEntity = albumReviewMapper.toEntity(albumReviewRequest, userEntity, albumEntity);

        AlbumReviewEntity savedEntity = albumReviewRepository.save(albumReviewEntity);

        return albumReviewMapper.toResponse(savedEntity);
    }

    private void validateIdentifiers(Long songId, String spotifyId) {
        if ((songId != null) == (spotifyId != null && !spotifyId.isBlank())) {
            throw new IllegalArgumentException("Exactly one identifier (songId or spotifyId) must be provided");
        }
    }

    private AlbumEntity findOrCreateAlbumEntity(Long albumId, String spotifyId) {
        if (albumId != null) {
            return albumRepository.findById(albumId)
                    .orElseThrow(() -> new EntityNotFoundException("Song with ID: " + albumId + " not found."));
        }

        if (spotifyId != null) {
            Optional<AlbumEntity> existingAlbum = albumRepository.findBySpotifyId(spotifyId);
            return existingAlbum.orElseGet(() -> createAlbumFromSpotify(spotifyId));
        }

        throw new IllegalArgumentException("Either songId or spotifyId must be provided");
    }

    private AlbumEntity createAlbumFromSpotify(String spotifyId) {
        AlbumRequest albumRequest = spotifyService.getAlbum(spotifyId);
        createEntitiesFromSpotify(albumRequest);
        AlbumEntity newAlbum = albumMapper.toEntity(albumRequest);
        return albumRepository.save(newAlbum);
    }

    private void createEntitiesFromSpotify(AlbumRequest albumRequest) {
        if(artistRepository.findBySpotifyId(albumRequest.getArtistSpotifyId()).isEmpty()){
            ArtistRequest artistRequest = spotifyService.getArtist(albumRequest.getArtistSpotifyId());
            ArtistEntity artistEntity = artistMapper.toEntity(artistRequest);
            artistRepository.save(artistEntity);
        }
    }

    public Page<AlbumReviewResponse> findByAlbumId(Long albumId, Pageable pageable) {
        if(albumReviewRepository.findByAlbum_AlbumId(albumId,pageable).isEmpty()){
            throw new EntityNotFoundException("Album with ID: " + albumId + " not found.");
        }
        return albumReviewMapper.toResponsePage(albumReviewRepository.findByAlbum_AlbumId(albumId, pageable));
    }

    public Page<AlbumReviewResponse> findBySpotifyId (String spotifyId, Pageable pageable){
        if(albumReviewRepository.findByAlbum_SpotifyId(spotifyId,pageable).isEmpty()){
            throw new EntityNotFoundException("Album with spotifyId: " + spotifyId + " not found.");
        }
        return albumReviewMapper.toResponsePage(albumReviewRepository.findByAlbum_SpotifyId(spotifyId,pageable));
    }

    public Page<AlbumReviewResponse> findByUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found."));
        return albumReviewMapper.toResponsePage(albumReviewRepository.findByUser_UserId(userId, pageable));
    }
}
