package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.CommentPatchRequest;
import com.musicspring.app.music_app.model.dto.request.CommentRequest;
import com.musicspring.app.music_app.model.dto.response.CommentResponse;
import com.musicspring.app.music_app.model.dto.response.ReactionResponse;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.enums.CommentType;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.model.mapper.CommentMapper;
import com.musicspring.app.music_app.model.mapper.ReactionMapper;
import com.musicspring.app.music_app.repository.*;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentMapper commentMapper;
    private final AlbumReviewRepository albumReviewRepository;
    private final SongReviewRepository songReviewRepository;
    private final ReactionRepository reactionRepository;
    private final ReactionMapper reactionMapper;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          ReviewRepository reviewRepository,
                          CommentMapper commentMapper,
                          AlbumReviewRepository albumReviewRepository,
                          SongReviewRepository songReviewRepository,
                          ReactionRepository reactionRepository,
                          ReactionMapper reactionMapper) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.commentMapper = commentMapper;
        this.albumReviewRepository = albumReviewRepository;
        this.songReviewRepository = songReviewRepository;
        this.reactionRepository = reactionRepository;
        this.reactionMapper = reactionMapper;
    }

    public Page<CommentResponse> findAll(Pageable pageable){
        return commentRepository.findAll(pageable)
                .map(this::enrichCommentResponse);
    }

    public CommentResponse findById(Long id){
        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID:" + id + " not found."));
        return enrichCommentResponse(commentEntity);
    }

    public void deleteById(Long id){
        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID:" + id + " not found."));

        AuthService.validateRequestUserOwnership(commentEntity.getUser().getUserId());

        commentEntity.setActive(false);
        commentRepository.save(commentEntity);
    }

    @Transactional
    public CommentResponse createSongReviewComment(CommentRequest commentRequest, Long reviewId) {
        AuthService.validateRequestUserOwnership(commentRequest.getUserId());

        UserEntity user = userRepository.findById(commentRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + commentRequest.getUserId() + " not found."));

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review with ID " + reviewId + " not found."));

        if (!(review instanceof SongReviewEntity)) {
            throw new IllegalArgumentException("Review with ID " + reviewId + " is not a song review.");
        }

        CommentEntity comment = commentMapper.toEntity(commentRequest, user, review, CommentType.SONG_REVIEW);
        comment.setActive(true);
        commentRepository.save(comment);

        return enrichCommentResponse(comment);
    }

    @Transactional
    public CommentResponse createAlbumReviewComment(CommentRequest commentRequest, Long reviewId) {
        AuthService.validateRequestUserOwnership(commentRequest.getUserId());

        UserEntity user = userRepository.findById(commentRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + commentRequest.getUserId() + " not found."));

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review with ID " + reviewId + " not found."));

        if (!(review instanceof AlbumReviewEntity)) {
            throw new IllegalArgumentException("Review with ID " + reviewId + " is not an album review.");
        }

        CommentEntity comment = commentMapper.toEntity(commentRequest, user, review, CommentType.ALBUM_REVIEW);
        comment.setActive(true);
        commentRepository.save(comment);

        return enrichCommentResponse(comment);
    }

    public CommentResponse updateCommentContent(Long commentId, CommentPatchRequest patchRequest) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID: " + commentId + " not found."));

        AuthService.validateRequestUserOwnership(commentEntity.getUser().getUserId());

        commentEntity.setText(patchRequest.getText());

        CommentEntity updated = commentRepository.save(commentEntity);

        return enrichCommentResponse(updated);
    }

    public Page<CommentResponse> findByUserId(Long userId, Pageable pageable){
        Page<CommentEntity> commentPage = commentRepository.findByUser_UserIdAndActiveTrue(userId, pageable);
        return commentPage.map(this::enrichCommentResponse);
    }

    public Page<CommentResponse> getCommentsByReviewId(Long reviewId, Pageable pageable) {
        if(albumReviewRepository.findById(reviewId).isEmpty() && songReviewRepository.findById(reviewId).isEmpty()){
            throw new EntityNotFoundException("Review with ID: " + reviewId + " not found.");
        }
        Page<CommentEntity> commentPage = commentRepository.findByReviewEntity_ReviewId(reviewId, pageable);
        return commentPage.map(this::enrichCommentResponse);
    }

    private CommentResponse enrichCommentResponse(CommentEntity comment) {
        Long commentId = comment.getCommentId();

        Long totalLikes = reactionRepository.countByComment_CommentIdAndReactionType(commentId, ReactionType.LIKE);
        Long totalDislikes = reactionRepository.countByComment_CommentIdAndReactionType(commentId, ReactionType.DISLIKE);
        Long totalLoves = reactionRepository.countByComment_CommentIdAndReactionType(commentId, ReactionType.LOVE);
        Long totalWows = reactionRepository.countByComment_CommentIdAndReactionType(commentId, ReactionType.WOW);

        ReactionResponse userReactionDto = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            CredentialEntity credential = (CredentialEntity) authentication.getPrincipal();
            UserEntity currentUser = credential.getUser();

            Optional<ReactionEntity> userReactionOpt = reactionRepository.findByUserAndComment(currentUser, comment);
            if (userReactionOpt.isPresent()) {
                userReactionDto = reactionMapper.toResponse(userReactionOpt.get());
            }
        }

        return commentMapper.toResponse(
                comment,
                totalLikes,
                totalDislikes,
                totalLoves,
                totalWows,
                userReactionDto
        );
    }

}
