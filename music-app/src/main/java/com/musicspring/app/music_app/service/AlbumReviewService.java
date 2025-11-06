package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.*;
import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.model.mapper.AlbumMapper;
import com.musicspring.app.music_app.model.mapper.AlbumReviewMapper;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
import com.musicspring.app.music_app.model.mapper.ReactionMapper;
import com.musicspring.app.music_app.repository.*;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.service.AuthService;
import com.musicspring.app.music_app.spotify.service.SpotifyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ReactionRepository reactionRepository;
    private final ReactionMapper reactionMapper;
    private final CommentRepository commentRepository;


    @Autowired
    public AlbumReviewService(AlbumReviewRepository albumReviewRepository,
                              AlbumRepository albumRepository,
                              UserRepository userRepository,
                              AlbumReviewMapper albumReviewMapper,
                              SpotifyService spotifyService,
                              AlbumMapper albumMapper,
                              ArtistMapper artistMapper,
                              ArtistRepository artistRepository,
                              ReactionRepository reactionRepository,
                              ReactionMapper reactionMapper,
                              CommentRepository commentRepository) {
        this.albumReviewRepository = albumReviewRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.albumReviewMapper = albumReviewMapper;
        this.spotifyService = spotifyService;
        this.albumMapper = albumMapper;
        this.artistMapper = artistMapper;
        this.artistRepository = artistRepository;
        this.reactionRepository = reactionRepository;
        this.reactionMapper = reactionMapper;
        this.commentRepository = commentRepository;
    }

    public Page<AlbumReviewResponse> findAll(Pageable pageable) {
        return albumReviewRepository.findAll(pageable)
                .map(this::enrichAlbumReviewResponse);
    }

    public AlbumReviewResponse findById(Long id) {
        AlbumReviewEntity review = albumReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album review with ID: " + id + " not found."));
        return enrichAlbumReviewResponse(review);
    }

    @Transactional
    public void deleteById(Long id) {
        AlbumReviewEntity albumReview = albumReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album review with ID: " + id + " not found."));

        AuthService.validateRequestUserOwnership(albumReview.getUser().getUserId());

        reactionRepository.deleteReactionsOnReviewComments(id);
        reactionRepository.deleteByReviewId(id);
        commentRepository.deactivateByReviewId(id);

        albumReview.setActive(false);
        albumReviewRepository.save(albumReview);
    }
    @Transactional
    public AlbumReviewResponse reactivateById(Long id) {
        AlbumReviewEntity albumReview = albumReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album review with ID: " + id + " not found."));

        AuthService.validateRequestUserOwnership(albumReview.getUser().getUserId());

        if (albumReview.getActive()) {
            throw new IllegalStateException("Album review is already active");
        }

        albumReview.setActive(true);
        albumReviewRepository.save(albumReview);

        commentRepository.reactivateByReviewId(id);

        return enrichAlbumReviewResponse(albumReview);
    }

    @Transactional
    public AlbumReviewResponse createAlbumReview(Long albumId, String spotifyId,
                                                 AlbumReviewRequest albumReviewRequest) {
        validateIdentifiers(albumId, spotifyId);

        if (albumReviewRequest.getUserId() == null) {
            throw new IllegalArgumentException("UserId is required");
        }

        Long authenticatedUserId = AuthService.extractUserId();
        validateUserOwnership(authenticatedUserId, albumReviewRequest.getUserId());

        UserEntity userEntity = userRepository.findById(albumReviewRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + albumReviewRequest.getUserId() + " not found."));

        AlbumEntity albumEntity = findOrCreateAlbumEntity(albumId, spotifyId);

        AlbumReviewEntity albumReviewEntity = albumReviewMapper.toEntity(albumReviewRequest, userEntity, albumEntity);
        AlbumReviewEntity savedEntity = albumReviewRepository.save(albumReviewEntity);

        return enrichAlbumReviewResponse(savedEntity);
    }

    private void validateUserOwnership(Long authenticatedUserId, Long requestedUserId) {
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AccessDeniedException("You can only create reviews for yourself");
        }
    }

    private void validateIdentifiers(Long albumId, String spotifyId) {
        if ((albumId != null) == (spotifyId != null && !spotifyId.isBlank())) {
            throw new IllegalArgumentException("Exactly one identifier (albumId or spotifyId) must be provided");
        }
    }
    private AlbumEntity findOrCreateAlbumEntity(Long albumId, String spotifyId) {
        if (albumId != null) {
            return albumRepository.findById(albumId)
                    .orElseThrow(() -> new EntityNotFoundException("Album with ID: " + albumId + " not found."));
        }

        if (spotifyId != null) {
            Optional<AlbumEntity> existingAlbum = albumRepository.findBySpotifyId(spotifyId);
            return existingAlbum.orElseGet(() -> createAlbumFromSpotify(spotifyId));
        }

        throw new IllegalArgumentException("Either albumId or spotifyId must be provided");
    }

    private AlbumEntity createAlbumFromSpotify(String spotifyId) {
        AlbumWithTracksResponse albumResponse = spotifyService.getAlbum(spotifyId); //aca hay error, ya que pide un albumResponse y tiene un AlbumWithTracksResponse

        if (artistRepository.findBySpotifyId(albumResponse.getArtistSpotifyId()).isEmpty()) {
            ArtistWithAlbumsResponse artistResponse = spotifyService.getArtist(albumResponse.getArtistSpotifyId());
            ArtistEntity artistEntity = artistMapper.withAlbumstoEntity(artistResponse);
            artistRepository.save(artistEntity);
        }

        AlbumEntity newAlbum = albumMapper.withTracksToEntity(albumResponse);
        newAlbum.setArtist(artistRepository.findBySpotifyId(albumResponse.getArtistSpotifyId())
                .orElseThrow(() -> new EntityNotFoundException("Artist not found")));

        return albumRepository.save(newAlbum);
    }
    public Page<AlbumReviewResponse> findByAlbum(Long albumId, String spotifyId, Pageable pageable) {
        validateIdentifiers(albumId, spotifyId);

        if (albumId != null) {
            albumRepository.findById(albumId).orElseThrow(() -> new EntityNotFoundException("Album with ID: " + albumId + " not found."));
            return albumReviewRepository.findByAlbum_AlbumId(albumId, pageable)
                    .map(this::enrichAlbumReviewResponse);
        } else {
            albumRepository.findBySpotifyId(spotifyId).orElseThrow(() -> new EntityNotFoundException("Album with spotifyId: " + spotifyId + " not found."));
            return albumReviewRepository.findByAlbum_SpotifyId(spotifyId, pageable)
                    .map(this::enrichAlbumReviewResponse);
        }
    }

    public Page<AlbumReviewResponse> findByUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found."));
        return albumReviewRepository.findByUser_UserIdAndActiveTrue(userId, pageable)
                .map(this::enrichAlbumReviewResponse);
    }

    public AlbumReviewResponse updateAlbumReviewContent(Long albumReviewId, ReviewPatchRequest patchRequest) {
        AlbumReviewEntity albumReviewEntity = albumReviewRepository.findById(albumReviewId)
                        .orElseThrow(() -> new EntityNotFoundException("Album review with ID: " + albumReviewId + " not found."));

        AuthService.validateRequestUserOwnership(albumReviewEntity.getUser().getUserId());

        albumReviewEntity.setDescription(patchRequest.getDescription());

        AlbumReviewEntity updated = albumReviewRepository.save(albumReviewEntity);

        return enrichAlbumReviewResponse(updated);
    }

    private AlbumReviewResponse enrichAlbumReviewResponse(AlbumReviewEntity review) {
        Long reviewId = review.getReviewId();

        Long totalLikes = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.LIKE);
        Long totalDislikes = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.DISLIKE);
        Long totalLoves = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.LOVE);
        Long totalWows = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.WOW);

        ReactionResponse userReactionDto = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            CredentialEntity credential = (CredentialEntity) authentication.getPrincipal();
            UserEntity currentUser = credential.getUser();

            Optional<ReactionEntity> userReactionOpt = reactionRepository.findByUserAndReview(currentUser, review);
            if (userReactionOpt.isPresent()) {
                userReactionDto = reactionMapper.toResponse(userReactionOpt.get());
            }
        }

        // 3. Usar el mapper sobrecargado
        return albumReviewMapper.toResponse(
                review,
                totalLikes,
                totalDislikes,
                totalLoves,
                totalWows,
                userReactionDto
        );
    }
}