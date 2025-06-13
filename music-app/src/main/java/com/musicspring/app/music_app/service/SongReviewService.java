package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.AlbumRequest;
import com.musicspring.app.music_app.model.dto.request.ArtistRequest;
import com.musicspring.app.music_app.model.dto.request.SongReviewRequest;
import com.musicspring.app.music_app.model.dto.response.SongReviewResponse;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.mapper.AlbumMapper;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
import com.musicspring.app.music_app.model.mapper.SongReviewMapper;
import com.musicspring.app.music_app.repository.*;
import com.musicspring.app.music_app.model.mapper.SongMapper;
import com.musicspring.app.music_app.spotify.service.SpotifyService;
import com.musicspring.app.music_app.model.dto.request.SongRequest;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SongReviewService {

    private final SongReviewRepository songReviewRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SongReviewMapper songReviewMapper;
    private final SongMapper songMapper;
    private final SpotifyService spotifyService;
    private final AlbumMapper albumMapper;
    private final AlbumRepository albumRepository;
    private final ArtistMapper artistMapper;
    private final ArtistRepository artistRepository;

    @Autowired
    public SongReviewService(SongReviewRepository songReviewRepository,
                             SongRepository songRepository,
                             UserRepository userRepository,
                             SongReviewMapper songReviewMapper,
                             SongMapper songMapper,
                             SpotifyService spotifyService, AlbumMapper albumMapper, AlbumRepository albumRepository, ArtistMapper artistMapper, ArtistRepository artistRepository) {
        this.songReviewRepository = songReviewRepository;
        this.songRepository = songRepository;
        this.userRepository = userRepository;
        this.songReviewMapper = songReviewMapper;
        this.songMapper = songMapper;
        this.spotifyService = spotifyService;
        this.albumMapper = albumMapper;
        this.albumRepository = albumRepository;
        this.artistMapper = artistMapper;
        this.artistRepository = artistRepository;
    }

    public Page<SongReviewResponse> findAll(Pageable pageable) {
        return songReviewMapper.toResponsePage(songReviewRepository.findAll(pageable));
    }

    public SongReviewResponse findById(Long id) {
        return songReviewMapper.toResponse(songReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song review with ID: " + id + " not found.")));
    }

    public void deleteById(Long id) {
        SongReviewEntity songReviewEntity = songReviewRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Song review with ID: " + id + " not found."));
        songReviewEntity.setActive(false);
        songReviewRepository.save(songReviewEntity);
    }

    public SongReviewResponse createSongReview(Long songId, String spotifyId,
                                               SongReviewRequest songReviewRequest) {

        validateIdentifiers(songId, spotifyId);

        if (songReviewRequest.getUserId() == null) {
            throw new IllegalArgumentException("UserId is required");
        }

        Long authenticatedUserId = extractUserId();
        validateUserOwnership(authenticatedUserId, songReviewRequest.getUserId());

        UserEntity userEntity = userRepository.findById(songReviewRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + songReviewRequest.getUserId() + " not found."));

        SongEntity songEntity = findOrCreateSongEntity(songId, spotifyId);

        SongReviewEntity songReviewEntity = songReviewMapper.toEntity(songReviewRequest, userEntity, songEntity);

        SongReviewEntity savedEntity = songReviewRepository.save(songReviewEntity);



        return songReviewMapper.toResponse(savedEntity);
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof CredentialEntity) {
            CredentialEntity credential = (CredentialEntity) authentication.getPrincipal();
            if (credential.getUser() != null) {
                return credential.getUser().getUserId();
            }
        }

        throw new AccessDeniedException("Unable to extract user ID from authentication");
    }

    private void validateUserOwnership(Long authenticatedUserId, Long requestedUserId) {
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AccessDeniedException("You can only create reviews for yourself");
        }
    }

    private void validateIdentifiers(Long songId, String spotifyId) {
        if ((songId != null) == (spotifyId != null && !spotifyId.isBlank())) {
            throw new IllegalArgumentException("Exactly one identifier (songId or spotifyId) must be provided");
        }
    }

    private SongEntity findOrCreateSongEntity(Long songId, String spotifyId) {
        if (songId != null) {
            return songRepository.findById(songId)
                    .orElseThrow(() -> new EntityNotFoundException("Song with ID: " + songId + " not found."));
        }

        if (spotifyId != null) {
            Optional<SongEntity> existingSong = songRepository.findBySpotifyId(spotifyId);
            return existingSong.orElseGet(() -> createSongFromSpotify(spotifyId));
        }

        throw new IllegalArgumentException("Either songId or spotifyId must be provided");
    }

    private SongEntity createSongFromSpotify(String spotifyId) {
        SongRequest songRequest = spotifyService.getSong(spotifyId);
        createEntitiesFromSpotify(songRequest);
        SongEntity newSong = songMapper.toEntity(songRequest);
        return songRepository.save(newSong);
    }

    private void createEntitiesFromSpotify(SongRequest songRequest) {
        if(albumRepository.findBySpotifyId(songRequest.getAlbumSpotifyId()).isEmpty()){
            AlbumRequest albumRequest = spotifyService.getAlbum(songRequest.getAlbumSpotifyId());
            AlbumEntity albumEntity = albumMapper.requestToEntity(albumRequest);
            albumRepository.save(albumEntity);
        }
        if(artistRepository.findBySpotifyId(songRequest.getArtistSpotifyId()).isEmpty()){
            ArtistRequest artistRequest = spotifyService.getArtist(songRequest.getArtistSpotifyId());
            ArtistEntity artistEntity = artistMapper.toEntity(artistRequest);
            artistRepository.save(artistEntity);
        }
    }

    public Page<SongReviewResponse> findBySong(Long songId, String spotifyId, Pageable pageable) {
        validateIdentifiers(songId, spotifyId);

        if (songId != null) {
            songRepository.findById(songId).orElseThrow(() -> new EntityNotFoundException("Song with ID: " + songId + " not found."));
            return songReviewMapper.toResponsePage(songReviewRepository.findBySong_Id(songId, pageable));
        } else {
            songRepository.findBySpotifyId(spotifyId).orElseThrow(() -> new EntityNotFoundException("Song with spotifyId: " + spotifyId + " not found."));
            return songReviewMapper.toResponsePage(songReviewRepository.findBySong_SpotifyId(spotifyId, pageable));
        }
    }

    public Page<SongReviewResponse> findBySongId (Long songId, Pageable pageable){
        return songReviewMapper.toResponsePage(songReviewRepository.findBySong_Id(songId,pageable));
    }

    public Page<SongReviewResponse> findBySpotifyId(String spotifyId, Pageable pageable) {
        return songReviewMapper.toResponsePage(songReviewRepository.findBySong_SpotifyId(spotifyId, pageable));
    }

    public Page<SongReviewResponse> findByUserId(Long songId, Pageable pageable){
        userRepository.findById(songId).orElseThrow(() -> new EntityNotFoundException("User with ID: " + songId + " not found."));
        return songReviewMapper.toResponsePage(songReviewRepository.findByUser_UserId(songId,pageable));
    }
}