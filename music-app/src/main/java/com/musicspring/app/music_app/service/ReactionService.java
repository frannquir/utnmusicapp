package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.ReactionRequest;
import com.musicspring.app.music_app.model.dto.response.ReactionResponse;
import com.musicspring.app.music_app.model.entity.CommentEntity;
import com.musicspring.app.music_app.model.entity.ReactionEntity;
import com.musicspring.app.music_app.model.entity.ReviewEntity;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.model.enums.ReactedType;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.model.mapper.ReactionMapper;
import com.musicspring.app.music_app.repository.*;
import com.musicspring.app.music_app.security.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final ReactionMapper reactionMapper;

    @Autowired
    public ReactionService(ReactionRepository reactionRepository,
                           UserRepository userRepository,
                           CommentRepository commentRepository,
                           ReviewRepository reviewRepository,
                           ReactionMapper reactionMapper) {
        this.reactionRepository = reactionRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
        this.reactionMapper = reactionMapper;
    }

    public Page<ReactionResponse> findAll(Pageable pageable){
        return reactionMapper.toResponsePage(reactionRepository.findAll(pageable));
    }

    public ReactionResponse findById(Long id){
        ReactionEntity reactionEntity = reactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reaction with ID:" + id + " not found."));
        return reactionMapper.toResponse(reactionEntity);
    }

    public Page<ReactionResponse> findByReviewId(Long reviewId, Pageable pageable) {
        Page<ReactionEntity> reactions = reactionRepository.findByReview_ReviewId(reviewId, pageable);
        return reactions.map(reactionMapper::toResponse);
    }

    public Page<ReactionResponse> findByCommentId(Long commentId, Pageable pageable) {
        Page<ReactionEntity> reactions = reactionRepository.findByComment_CommentId(commentId, pageable);
        return reactions.map(reactionMapper::toResponse);
    }

    public Page<ReactionResponse> findReactionsByTypeAndTarget(ReactionType reactionType, ReactedType reactedType, Pageable pageable) {
        Page<ReactionEntity> reactions = reactionRepository.findByReactionTypeAndReactedType(reactionType, reactedType, pageable);
        return reactions.map(reactionMapper::toResponse);
    }

    public Page<ReactionResponse> findReactionsByUserId(Long userId, Pageable pageable) {
        Page<ReactionEntity> reactions = reactionRepository.findByUser_UserId(userId, pageable);
        return reactions.map(reactionMapper::toResponse);
    }

    @Transactional
    public ReactionResponse createReviewReaction(ReactionRequest reactionRequest, Long reviewId) {
        AuthService.validateRequestUserOwnership(reactionRequest.getUserId());

        UserEntity user = userRepository.findById(reactionRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + reactionRequest.getUserId() + " not found."));

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review with ID " + reviewId + " not found."));

        reactionRepository.findByUserAndReview(user, review).ifPresent(r -> {
            throw new IllegalStateException("User already has a reaction on this review.");
        });

        ReactionEntity reaction = reactionMapper.toEntity(reactionRequest, user, review);
        reactionRepository.save(reaction);

        return reactionMapper.toResponse(reaction);
    }

    @Transactional
    public ReactionResponse createCommentReaction(ReactionRequest reactionRequest, Long commentId) {
        AuthService.validateRequestUserOwnership(reactionRequest.getUserId());

        UserEntity user = userRepository.findById(reactionRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + reactionRequest.getUserId() + " not found."));

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID " + commentId + " not found."));

        reactionRepository.findByUserAndComment(user, comment).ifPresent(r -> {
            throw new IllegalStateException("User already has a reaction on this comment.");
        });

        ReactionEntity reaction = reactionMapper.toEntity(reactionRequest, user, comment);
        reactionRepository.save(reaction);

        return reactionMapper.toResponse(reaction);
    }



    @Transactional
    public ReactionResponse updateReaction(Long reactionId, ReactionType newReactionType) {

        ReactionEntity reactionEntity = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new EntityNotFoundException("Reaction with ID " + reactionId + " not found."));

        Long reactionOwnerId = reactionEntity.getUser().getUserId();
        AuthService.validateRequestUserOwnership(reactionOwnerId);

        reactionEntity.setReactionType(newReactionType);
        ReactionEntity updated = reactionRepository.save(reactionEntity);
        return reactionMapper.toResponse(updated);
    }


    @Transactional
    public void deleteReaction(Long reactionId) {
        reactionRepository.findById(reactionId).ifPresent(reactionEntity -> {
            AuthService.validateRequestUserOwnership(reactionEntity.getUser().getUserId());
            reactionRepository.deleteById(reactionId);
        });
    }

}
