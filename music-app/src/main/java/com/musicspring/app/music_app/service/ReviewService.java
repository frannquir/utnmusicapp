package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.entity.AlbumReviewEntity;
import com.musicspring.app.music_app.model.entity.ReviewEntity;
import com.musicspring.app.music_app.model.entity.SongReviewEntity;
import com.musicspring.app.music_app.model.mapper.AlbumReviewMapper;
import com.musicspring.app.music_app.model.mapper.SongReviewMapper;
import com.musicspring.app.music_app.repository.CommentRepository;
import com.musicspring.app.music_app.repository.ReactionRepository;
import com.musicspring.app.music_app.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final AlbumReviewMapper albumReviewMapper;
    private final SongReviewMapper songReviewMapper;

    public ReviewService(ReviewRepository reviewRepository, ReactionRepository reactionRepository, CommentRepository commentRepository, AlbumReviewMapper albumReviewMapper, SongReviewMapper songReviewMapper) {
        this.reviewRepository = reviewRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.albumReviewMapper = albumReviewMapper;
        this.songReviewMapper = songReviewMapper;
    }

    public Page<Object> getAllReviews (Pageable pageable){
        Page<ReviewEntity> reviews = reviewRepository.findAll(pageable);

        return reviews.map(review -> {
            if(review instanceof AlbumReviewEntity){
                return albumReviewMapper.toResponse((AlbumReviewEntity) review);
            } else if (review instanceof SongReviewEntity) {
                return songReviewMapper.toResponse((SongReviewEntity) review);
            }
            return null;
        });
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review with ID: " + reviewId + " not found."));

        reactionRepository.deleteReactionsOnReviewComments(reviewId);
        commentRepository.deactivateByReviewId(reviewId);
        reactionRepository.deleteByReviewId(reviewId);

        reviewEntity.setActive(false);
        reviewRepository.save(reviewEntity);
    }

    @Transactional
    public void reActivateReview (Long reviewId){
        ReviewEntity reviewEntity = reviewRepository.findByIdInactive(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review with ID: " + reviewId + " not found."));

        System.out.println(reviewEntity);

        if (reviewEntity.getActive()) {
            throw new IllegalStateException("The review is already active.");
        }

        if (!reviewEntity.getUser().getActive()) {
            throw new IllegalStateException("Cannot reactivate review. The owner (User ID: "
                    + reviewEntity.getUser().getUserId() + ") is inactive.");
        }

        reviewEntity.setActive(true);
        reviewRepository.save(reviewEntity);
        commentRepository.reactivateCommentByReviewId(reviewId);
    }
}