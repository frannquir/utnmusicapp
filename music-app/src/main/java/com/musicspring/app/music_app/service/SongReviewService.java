package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.exception.DuplicateReviewException;
import com.musicspring.app.music_app.model.dto.request.*;
import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.model.mapper.*;
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
    private final ReactionRepository reactionRepository;
    private final ReactionMapper reactionMapper;
    private final CommentRepository commentRepository;

    @Autowired
    public SongReviewService(SongReviewRepository songReviewRepository,
                             SongRepository songRepository,
                             UserRepository userRepository,
                             SongReviewMapper songReviewMapper,
                             SongMapper songMapper,
                             SpotifyService spotifyService,
                             AlbumMapper albumMapper,
                             AlbumRepository albumRepository,
                             ArtistMapper artistMapper,
                             ArtistRepository artistRepository,
                             ReactionRepository reactionRepository,
                             ReactionMapper reactionMapper,
                             CommentRepository commentRepository) {
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
        this.reactionRepository = reactionRepository;
        this.reactionMapper = reactionMapper;
        this.commentRepository = commentRepository;
    }

    public Page<SongReviewResponse> findAll(Pageable pageable) {
        return songReviewRepository.findAll(pageable)
                .map(this::enrichSongReviewResponse);
    }

    public SongReviewResponse findById(Long id) {
        SongReviewEntity review = songReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        return enrichSongReviewResponse(review);
    }

    @Transactional
    public void deleteById(Long id) {
        SongReviewEntity songReview = songReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song review with ID: " + id + " not found."));

        AuthService.validateRequestUserOwnership(songReview.getUser().getUserId());

        reactionRepository.deleteReactionsOnReviewComments(id);
        reactionRepository.deleteByReviewId(id);
        commentRepository.deactivateByReviewId(id);

        songReview.setActive(false);
        songReviewRepository.save(songReview);
    }
    @Transactional
    public SongReviewResponse reactivateById(Long id) {
        SongReviewEntity songReview = songReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song review with ID: " + id + " not found."));

        AuthService.validateRequestUserOwnership(songReview.getUser().getUserId());

        if (songReview.getActive()) {
            throw new IllegalStateException("Song review is already active");
        }

        songReview.setActive(true);
        songReviewRepository.save(songReview);


        commentRepository.reactivateByReviewId(id);

        return enrichSongReviewResponse(songReview);
    }
    @Transactional
    public SongReviewResponse createSongReview(Long songId, String spotifyId,
                                               SongReviewRequest songReviewRequest) {

        validateIdentifiers(songId, spotifyId);

        if (songReviewRequest.getUserId() == null) {
            throw new IllegalArgumentException("UserId is required");
        }

        Long authenticatedUserId = AuthService.extractUserId();
        validateUserOwnership(authenticatedUserId, songReviewRequest.getUserId());

        UserEntity userEntity = userRepository.findById(songReviewRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + songReviewRequest.getUserId() + " not found."));

        SongEntity songEntity = findOrCreateSongEntity(songId, spotifyId);

        // Check if user has already reviewed this song
        Optional<SongReviewEntity> existingReview = songReviewRepository
                .findByUserUserIdAndSongSongIdAndActiveTrue(userEntity.getUserId(), songEntity.getSongId());
        if (existingReview.isPresent()) {
            throw new DuplicateReviewException("User has already reviewed this song");
        }

        SongReviewEntity songReviewEntity = songReviewMapper.toEntity(songReviewRequest, userEntity, songEntity);
        SongReviewEntity savedEntity = songReviewRepository.save(songReviewEntity);

        return enrichSongReviewResponse(savedEntity);
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
        SongResponse songResponse = spotifyService.getSong(spotifyId);

        if (artistRepository.findBySpotifyId(songResponse.getArtistSpotifyId()).isEmpty()) {
//            ArtistResponse artistResponse = spotifyService.getArtist(songResponse.getArtistSpotifyId());
            ArtistWithAlbumsResponse artistResponse = spotifyService.getArtist(songResponse.getArtistSpotifyId());
            ArtistEntity artistEntity = artistMapper.withAlbumstoEntity(artistResponse);
            artistRepository.save(artistEntity);
        }

        if (albumRepository.findBySpotifyId(songResponse.getAlbumSpotifyId()).isEmpty()) {
            AlbumWithTracksResponse albumResponse = spotifyService.getAlbum(songResponse.getAlbumSpotifyId());
            AlbumEntity albumEntity = albumMapper.withTracksToEntity(albumResponse);
            albumEntity.setArtist(artistRepository.findBySpotifyId(songResponse.getArtistSpotifyId())
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found")));
            albumRepository.save(albumEntity);
        }

        SongEntity newSong = songMapper.toEntity(songResponse);
        newSong.setAlbum(albumRepository.findBySpotifyId(songResponse.getAlbumSpotifyId())
                .orElseThrow(() -> new EntityNotFoundException("Album not found")));

        return songRepository.save(newSong);
    }
    public Page<SongReviewResponse> findBySong(Long songId, String spotifyId, Pageable pageable) {
        validateIdentifiers(songId, spotifyId);

        if (songId != null) {
            songRepository.findById(songId).orElseThrow(() -> new EntityNotFoundException("Song with ID: " + songId + " not found."));
            return songReviewRepository.findBySong_Id(songId, pageable)
                    .map(this::enrichSongReviewResponse);
        } else {
            songRepository.findBySpotifyId(spotifyId).orElseThrow(() -> new EntityNotFoundException("Song with spotifyId: " + spotifyId + " not found."));
            return songReviewRepository.findBySong_SpotifyId(spotifyId, pageable)
                    .map(this::enrichSongReviewResponse);
        }
    }

    public Page<SongReviewResponse> findByUserId(Long userId, Pageable pageable){
        Page<SongReviewEntity> page = songReviewRepository.findByUser_UserIdAndActiveTrue(userId, pageable);
        return page.map(this::enrichSongReviewResponse);
    }

    public SongReviewResponse updateSongReviewContent(Long songReviewId, ReviewPatchRequest patchRequest) {
        SongReviewEntity songReviewEntity = songReviewRepository.findById(songReviewId)
                .orElseThrow(() -> new EntityNotFoundException("Song review with ID: " + songReviewId + " not found."));

        AuthService.validateRequestUserOwnership(songReviewEntity.getUser().getUserId());

        songReviewEntity.setDescription(patchRequest.getDescription());

        SongReviewEntity updated = songReviewRepository.save(songReviewEntity);

        return enrichSongReviewResponse(updated);
    }

    private SongReviewResponse enrichSongReviewResponse(SongReviewEntity review) {
        Long reviewId = review.getReviewId();

        Long totalLikes = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.LIKE);
        Long totalDislikes = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.DISLIKE);
        Long totalLoves = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.LOVE);
        Long totalWows = reactionRepository.countByReview_ReviewIdAndReactionType(reviewId, ReactionType.WOW);
        Long totalComments = commentRepository.countByReviewEntity_ReviewIdAndActiveTrue(reviewId);

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

        return songReviewMapper.toResponse(
                review,
                totalLikes,
                totalDislikes,
                totalLoves,
                totalWows,
                userReactionDto,
                totalComments
        );
    }
}