package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.model.dto.request.*;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.mapper.AlbumReviewMapper;
import com.musicspring.app.music_app.repository.*;
import com.musicspring.app.music_app.model.mapper.SongReviewMapper;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.model.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlbumReviewRepository albumReviewRepository;
    private final AlbumReviewMapper albumReviewMapper;
    private final SongReviewRepository songReviewRepository;
    private final SongReviewMapper songReviewMapper;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       CredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       AlbumReviewRepository albumReviewRepository,
                       SongReviewRepository songReviewRepository,
                       AlbumReviewMapper albumReviewMapper,
                       SongReviewMapper songReviewMapper, CommentRepository commentRepository, ReactionRepository reactionRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.albumReviewRepository = albumReviewRepository;
        this.albumReviewMapper = albumReviewMapper;
        this.songReviewMapper = songReviewMapper;
        this.songReviewRepository = songReviewRepository;
        this.commentRepository = commentRepository;
        this.reactionRepository = reactionRepository;
    }


    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }


    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " was not found."));
    }

    public UserEntity findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " was not found."));
    }

    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("User with Username: " + username + " was not found."));
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("User with ID: " + id + " was not found."));
        deleteReviewsAndComments(user);
        user.setActive(false);
        userRepository.save(user);
//        userRepository.deleteById(id);
    }

    private void deleteReviewsAndComments(UserEntity user){
        List<AlbumReviewEntity> reviews = albumReviewRepository
                .findByUser_UserId(user.getUserId(), Pageable.unpaged())
                .getContent();
        reviews.stream().forEach(review -> review.setActive(false));
        albumReviewRepository.saveAll(reviews);

        List<SongReviewEntity> songReviews = songReviewRepository
                .findByUser_UserId(user.getUserId(), Pageable.unpaged())
                .getContent();
        songReviews.stream().forEach(review -> review.setActive(false));
        songReviewRepository.saveAll(songReviews);

        List<CommentEntity> comments = commentRepository
                .findByUser_UserId(user.getUserId(), Pageable.unpaged())
                .getContent();
        comments.stream().forEach(comment -> comment.setActive(false));
        commentRepository.saveAll(comments);

        List<ReactionEntity> reactions = reactionRepository
                .findByUser_UserId(user.getUserId(),Pageable.unpaged())
                .getContent();
        reactionRepository.deleteAll(reactions);
    }

    @Transactional
    public void reactivateUser(Long id) {
        UserEntity user = userRepository.findByIdAndActiveFalse(id).orElseThrow(()
                -> new EntityNotFoundException("User with ID: " + id + " was not found."));
        user.setActive(true);
        userRepository.save(user);
        reactivateReviewsAndComments(user);
    }

    private void reactivateReviewsAndComments(UserEntity user){
        List<AlbumReviewEntity> reviews = albumReviewRepository
                .findByUser_UserIdAndActiveFalse(user.getUserId(), Pageable.unpaged())
                .getContent();
        reviews.stream().forEach(review -> review.setActive(true));
        albumReviewRepository.saveAll(reviews);

        List<SongReviewEntity> songReviews = songReviewRepository
                .findByUser_UserIdAndActiveFalse(user.getUserId(), Pageable.unpaged())
                .getContent();
        songReviews.stream().forEach(review -> review.setActive(true));
        songReviewRepository.saveAll(songReviews);

        List<CommentEntity> comments = commentRepository
                .findByUser_UserIdAndActiveFalse(user.getUserId(), Pageable.unpaged())
                .getContent();
        comments.stream().forEach(comment -> comment.setActive(true));
        commentRepository.saveAll(comments);
    }

    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        Page<UserEntity> userPage = userRepository.findByUsernameContainingIgnoreCase(query, pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {

        UserEntity existingUser = findEntityById(id);
        // Update only the present fields
        if (updateRequest.getUsername() != null &&
                !updateRequest.getUsername().equals(existingUser.getUsername())) {

            if (userRepository.existsByUsernameAndUserIdNot(updateRequest.getUsername(), id))
                throw new IllegalArgumentException("Username already taken: " + updateRequest.getUsername());
            existingUser.setUsername(updateRequest.getUsername());
        }

        CredentialEntity credential = existingUser.getCredential();

        if (updateRequest.getProfilePictureUrl() != null)
            credential.setProfilePictureUrl(updateRequest.getProfilePictureUrl());

        if (updateRequest.getActive() != null)
            existingUser.setActive(updateRequest.getActive());

        UserEntity savedUser = userRepository.save(existingUser);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public void updatePassword(Long id, PasswordUpdateRequest passwordRequest, Authentication authentication) {
        UserEntity user = findEntityById(id);
        CredentialEntity credential = user.getCredential();

        if (credential == null)
            throw new IllegalStateException("User has no credentials");


        // Verify current password
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), credential.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");

        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword()))
            throw new IllegalArgumentException("New password and confirmation do not match");

        credential.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        credentialRepository.save(credential);
    }

    @Transactional
    public UserResponse updateProfile(Long id, ProfileUpdateRequest profileRequest) {
        UserEntity user = findEntityById(id);
        CredentialEntity credential = user.getCredential();

        if (credential != null) {
            if (profileRequest.getProfilePictureUrl() != null) {
                credential.setProfilePictureUrl(profileRequest.getProfilePictureUrl());
            }

            if (profileRequest.getBiography() != null) {
                credential.setBiography(profileRequest.getBiography());
            }

            credentialRepository.save(credential);
        }

        return userMapper.toResponse(user);
    }

    public UserResponse getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        CredentialEntity credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toResponse(credential.getUser());
    }

    public UserProfileResponse getUserProfile(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + username + " was not found."));

        long albumReviewCount = albumReviewRepository.countByUser_UserId(user.getUserId());
        long songReviewCount = songReviewRepository.countByUser_UserId(user.getUserId());

        Double avgRating = calculateUserAverageRating(user.getUserId());

        UserProfileResponse userProfile = userMapper.toUserProfile(user);
        userProfile.setTotalAlbumReviews(albumReviewCount);
        userProfile.setTotalSongReviews(songReviewCount);
        userProfile.setAverageRating(avgRating);
        return userProfile;
    }

    public Page<AlbumReviewResponse> getUserAlbumReviews(String username, Pageable pageable) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + username + " was not found."));

        Page<AlbumReviewEntity> reviewPage = albumReviewRepository.findByUser_UserId(user.getUserId(), pageable);
        return albumReviewMapper.toResponsePage(reviewPage);
    }

    public Page<SongReviewResponse> getUserSongReviews(String username, Pageable pageable) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + username + " was not found."));

        Page<SongReviewEntity> reviewPage = songReviewRepository.findByUser_UserId(user.getUserId(), pageable);
        return songReviewMapper.toResponsePage(reviewPage);
    }

    private Double calculateUserAverageRating(Long userId) {
        Double average = userRepository.calculateUserAverageRating(userId);
        return average != null ? average : 0.0;
    }

}