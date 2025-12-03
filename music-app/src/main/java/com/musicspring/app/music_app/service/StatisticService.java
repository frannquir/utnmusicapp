package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.model.enums.CommentType;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.model.mapper.AlbumMapper;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
import com.musicspring.app.music_app.model.mapper.SongMapper;
import com.musicspring.app.music_app.model.mapper.UserMapper;
import com.musicspring.app.music_app.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;


@Service
public class StatisticService {
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final SongMapper songMapper;
    private final AlbumMapper albumMapper;
    private final ArtistMapper artistMapper;
    private final UserMapper userMapper;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final AlbumReviewRepository albumReviewRepository;
    private final SongReviewRepository songReviewRepository;

    public StatisticService(AlbumRepository albumRepository, SongRepository songRepository, 
                           SongMapper songMapper, AlbumMapper albumMapper, ArtistMapper artistMapper, UserMapper userMapper,
                           ArtistRepository artistRepository, UserRepository userRepository,
                           ReviewRepository reviewRepository, ReactionRepository reactionRepository,
                           CommentRepository commentRepository, AlbumReviewRepository albumReviewRepository,
                           SongReviewRepository songReviewRepository) {
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.songMapper = songMapper;
        this.albumMapper = albumMapper;
        this.artistMapper = artistMapper;
        this.userMapper = userMapper;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.albumReviewRepository = albumReviewRepository;
        this.songReviewRepository = songReviewRepository;
    }

    public Page<SongResponse> getMostReviewedSongs(Pageable pageable) {
        return songMapper.toResponsePage(songRepository.findTopReviewedSongs(pageable));
    }

    public Page<SongResponse> getTopSongsByReactionType(ReactionType reactionType, Pageable pageable) {
        return songMapper.toResponsePage(songRepository.findMostReactedSongs(reactionType, pageable));
    }

    public Page<AlbumResponse> getMostReviewedAlbums(Pageable pageable) {
        return albumMapper.toResponsePage(albumRepository.findTopReviewedAlbums(pageable));
    }

    public Page<AlbumResponse> getTopAlbumsByReactionType(ReactionType reactionType, Pageable pageable) {
        return albumMapper.toResponsePage(albumRepository.findMostReactedAlbums(reactionType, pageable));
    }

    public Page<ArtistResponse> getTopArtistsByReactionType(ReactionType reactionType, Pageable pageable) {
        return artistMapper.toResponsePage(artistRepository.findTopArtistsByReactionType(reactionType, pageable));
    }

    public Page<ArtistResponse> getMostReviewedArtists(Pageable pageable){
        return artistMapper.toResponsePage(artistRepository.findMostReviewedArtists(pageable));
    }
    
    public UserStatsResponse getUserStatistics(Long userId) {
    
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        
        Long totalAlbumReviews = albumReviewRepository.countByUserUserId(userId);
        Long totalSongReviews = songReviewRepository.countByUserUserId(userId);
        Long totalReviews = reviewRepository.countTotalReviewsByUserId(userId);
        Double averageRating = userRepository.calculateUserAverageRating(userId);
        
        Long totalComments = commentRepository.countByUserUserId(userId);
        Long albumComments = commentRepository.countCommentsByUserAndType(userId, CommentType.ALBUM_REVIEW);
        Long songComments = commentRepository.countCommentsByUserAndType(userId, CommentType.SONG_REVIEW);
        
        Long totalReactions = reactionRepository.countByUserUserId(userId);
        Long likesGiven = reactionRepository.countReactionsByUserAndType(userId, ReactionType.LIKE);
        Long lovesGiven = reactionRepository.countReactionsByUserAndType(userId, ReactionType.LOVE);
        Long wowsGiven = reactionRepository.countReactionsByUserAndType(userId, ReactionType.WOW);
        Long dislikesGiven = reactionRepository.countReactionsByUserAndType(userId, ReactionType.DISLIKE);
        
        Long likesReceived = reactionRepository.countReactionsReceivedOnReviews(userId, ReactionType.LIKE) +
                            reactionRepository.countReactionsReceivedOnComments(userId, ReactionType.LIKE);
        Long lovesReceived = reactionRepository.countReactionsReceivedOnReviews(userId, ReactionType.LOVE) +
                            reactionRepository.countReactionsReceivedOnComments(userId, ReactionType.LOVE);
        Long wowsReceived = reactionRepository.countReactionsReceivedOnReviews(userId, ReactionType.WOW) +
                           reactionRepository.countReactionsReceivedOnComments(userId, ReactionType.WOW);
        Long dislikesReceived = reactionRepository.countReactionsReceivedOnReviews(userId, ReactionType.DISLIKE) +
                               reactionRepository.countReactionsReceivedOnComments(userId, ReactionType.DISLIKE);
        
        Long reviewsThisMonth = reviewRepository.countReviewsThisMonth(userId, startOfMonth);
        Long commentsThisMonth = commentRepository.countCommentsThisMonth(userId, startOfMonth);
        Long reactionsThisMonth = reactionRepository.countReactionsThisMonth(userId, startOfMonth);

        return new UserStatsResponse(
                totalAlbumReviews, totalSongReviews, totalReviews, averageRating,
                totalComments, albumComments, songComments,
                totalReactions, likesGiven, lovesGiven, wowsGiven, dislikesGiven,
                likesReceived, lovesReceived, wowsReceived, dislikesReceived,
                reviewsThisMonth, commentsThisMonth, reactionsThisMonth
        );
    }

    public AdminDashboardResponse getAdminDashboard() {
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByActiveTrue();
        Long bannedUsers = userRepository.countByIsBannedTrue();

        Long totalReviews = reviewRepository.count();
        Long totalComments = commentRepository.countByActiveTrue();
        Long totalReactions = reactionRepository.count();

        Long albumReviews = albumReviewRepository.count();
        Long songReviews = songReviewRepository.count();

        Long likes = reactionRepository.countByReactionType(ReactionType.LIKE);
        Long loves = reactionRepository.countByReactionType(ReactionType.LOVE);
        Long wows = reactionRepository.countByReactionType(ReactionType.WOW);
        Long dislikes = reactionRepository.countByReactionType(ReactionType.DISLIKE);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .bannedUsers(bannedUsers)
                .totalReviews(totalReviews)
                .totalComments(totalComments)
                .totalReactions(totalReactions)
                .albumReviews(albumReviews)
                .songReviews(songReviews)
                .likesCount(likes)
                .lovesCount(loves)
                .wowsCount(wows)
                .dislikesCount(dislikes)
                .build();
    }
}