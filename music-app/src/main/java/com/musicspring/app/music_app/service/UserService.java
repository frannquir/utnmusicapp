package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.model.dto.request.*;
import com.musicspring.app.music_app.model.entity.AlbumReviewEntity;
import com.musicspring.app.music_app.model.mapper.AlbumReviewMapper;
import com.musicspring.app.music_app.repository.AlbumReviewRepository;
import com.musicspring.app.music_app.model.entity.SongReviewEntity;
import com.musicspring.app.music_app.model.mapper.SongReviewMapper;
import com.musicspring.app.music_app.repository.SongReviewRepository;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.model.mapper.UserMapper;
import com.musicspring.app.music_app.repository.UserRepository;
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

    @Autowired
    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       CredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       AlbumReviewRepository albumReviewRepository,
                       SongReviewRepository songReviewRepository,
                       AlbumReviewMapper albumReviewMapper,
                       SongReviewMapper songReviewMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.albumReviewRepository = albumReviewRepository;
        this.albumReviewMapper = albumReviewMapper;
        this.songReviewMapper = songReviewMapper;
        this.songReviewRepository = songReviewRepository;
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

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
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