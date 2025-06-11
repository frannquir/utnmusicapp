package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.request.CommentPatchRequest;
import com.musicspring.app.music_app.model.dto.request.CommentRequest;
import com.musicspring.app.music_app.model.dto.response.CommentResponse;
import com.musicspring.app.music_app.model.entity.CommentEntity;
import com.musicspring.app.music_app.model.entity.ReviewEntity;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.model.enums.CommentType;
import com.musicspring.app.music_app.model.mapper.CommentMapper;
import com.musicspring.app.music_app.repository.AlbumReviewRepository;
import com.musicspring.app.music_app.repository.CommentRepository;
import com.musicspring.app.music_app.repository.SongReviewRepository;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SongReviewRepository songReviewRepository;
    private final AlbumReviewRepository albumReviewRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          SongReviewRepository songReviewRepository,
                          AlbumReviewRepository albumReviewRepository,
                          CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.songReviewRepository = songReviewRepository;
        this.albumReviewRepository = albumReviewRepository;
        this.commentMapper = commentMapper;
    }

    public Page<CommentResponse> findAll(Pageable pageable){
        return commentMapper.toResponsePage(commentRepository.findAll(pageable));
    }

    public CommentResponse findById(Long id){
        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID:" + id + " not found."));
        return commentMapper.toResponse(commentEntity);
    }

    public void deleteById(Long id){
        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID:" + id + " not found."));

        AuthService.validateRequestUserOwnership(commentEntity.getUser().getUserId());

        commentRepository.delete(commentEntity);
    }

    public CommentResponse createComment(Long reviewId, CommentRequest commentRequest) {
        if (commentRequest.getUserId() == null) {
            throw new IllegalArgumentException("UserId is required");
        }
        if (commentRequest.getCommentType() == null) {
            throw new IllegalArgumentException("CommentType is required");
        }

        AuthService.validateRequestUserOwnership(commentRequest.getUserId());

        UserEntity userEntity = userRepository.findById(commentRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID:" + commentRequest.getUserId() + " not found."));

        ReviewEntity reviewEntity = switch (commentRequest.getCommentType()) {
            case SONG_REVIEW -> songReviewRepository.findById(reviewId)
                    .orElseThrow(() -> new EntityNotFoundException("Song Review with ID " + reviewId + " not found."));
            case ALBUM_REVIEW -> albumReviewRepository.findById(reviewId)
                    .orElseThrow(() -> new EntityNotFoundException("Album Review with ID " + reviewId + " not found."));
        };

        CommentEntity commentEntity = commentMapper.toEntity(commentRequest, userEntity, reviewEntity);

        CommentEntity saved = commentRepository.save(commentEntity);

        return commentMapper.toResponse(saved);
    }

    public CommentResponse updateCommentContent(Long commentId, CommentPatchRequest patchRequest) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment with ID: " + commentId + " not found."));

        AuthService.validateRequestUserOwnership(commentEntity.getUser().getUserId());

        commentEntity.setText(patchRequest.getText());

        CommentEntity updated = commentRepository.save(commentEntity);

        return commentMapper.toResponse(updated);
    }


    public Page<CommentResponse> findByUserId(Long userId, Pageable pageable){
        return commentMapper.toResponsePage(commentRepository.findByUser_UserId(userId, pageable));
    }

    public Page<CommentResponse> getCommentsByReviewId(Long reviewId, Pageable pageable) {
        Page<CommentEntity> commentPage = commentRepository.findByReviewEntity_ReviewId(reviewId, pageable);
        return commentMapper.toResponsePage(commentPage);
    }

    public Page<CommentResponse> getCommentsByReviewIdAndType(Long reviewId, CommentType commentType, Pageable pageable) {
        Page<CommentEntity> commentPage = commentRepository.findByReviewEntity_ReviewIdAndCommentType(reviewId, commentType, pageable);
        return commentMapper.toResponsePage(commentPage);
    }



}
