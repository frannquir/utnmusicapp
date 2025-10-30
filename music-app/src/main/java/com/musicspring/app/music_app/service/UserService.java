package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.model.dto.request.*;
import com.musicspring.app.music_app.model.entity.*;
import com.musicspring.app.music_app.model.mapper.AlbumReviewMapper;
import com.musicspring.app.music_app.repository.*;
import com.musicspring.app.music_app.model.mapper.SongReviewMapper;
import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.dto.CompleteProfileRequest;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.mapper.AuthMapper;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.model.mapper.UserMapper;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.security.service.AuthService;
import com.musicspring.app.music_app.security.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
    private final StatisticService statisticService;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       CredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder,
                       AlbumReviewRepository albumReviewRepository,
                       SongReviewRepository songReviewRepository,
                       AlbumReviewMapper albumReviewMapper,
                       SongReviewMapper songReviewMapper,
                       CommentRepository commentRepository,
                       ReactionRepository reactionRepository,
                       StatisticService statisticService,
                       JwtService jwtService,
                       AuthMapper authMapper,
                       RoleRepository roleRepository) {
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
        this.statisticService = statisticService;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.roleRepository = roleRepository;
    }


    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserProfileResponse)
                .collect(Collectors.toList());
    }


    public UserProfileResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toUserProfileResponse)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " was not found."));
    }

    public UserEntity findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + id + " was not found."));
    }

    public UserProfileResponse getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with Username: " + username + " was not found."));

        UserStatsResponse stats = statisticService.getUserStatistics(user.getUserId());

        return userMapper.toUserProfileResponse(user, stats); 
    }

    @Transactional
    public void deleteUser(Long id) {
        Long authenticatedUserId = AuthService.extractUserId();
        validateUserOwnership(authenticatedUserId,id);

        UserEntity user = userRepository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("User with ID: " + id + " was not found."));

        commentRepository.deactivateCommentsOnUserReviews(id);
        albumReviewRepository.deactivateByUserId(id);
        songReviewRepository.deactivateByUserId(id);
        commentRepository.deactivateByUserId(id);
        reactionRepository.deleteByUserId(id);

        user.setActive(false);
        userRepository.save(user);
    }

    private void validateUserOwnership(Long authenticatedUserId, Long requestedUserId) {
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AccessDeniedException("You can only deactivate your account");
        }
    }

    @Transactional
    public void reactivateUser(Long id) {
        UserEntity user = userRepository.findByIdAndActiveFalse(id).orElseThrow(()
                -> new EntityNotFoundException("User with ID: " + id + " was not found."));

        user.setActive(true);
        userRepository.save(user);

        albumReviewRepository.reactivateByUserId(id);
        songReviewRepository.reactivateByUserId(id);
        commentRepository.reactivateByUserId(id);
        commentRepository.reactivateCommentsOnUserReviews(id);
    }

    public Page<UserProfileResponse> searchUsers(String query, Pageable pageable) {
        Page<UserEntity> userPage = userRepository.findByUsernameContainingIgnoreCase(query, pageable);
        return userPage.map(userMapper::toUserProfileResponse);
    }

    @Transactional
    public UserProfileResponse updateUser(Long id, UserUpdateRequest updateRequest) {

        UserEntity existingUser = findEntityById(id);
        if (updateRequest.getUsername() != null &&
                !updateRequest.getUsername().equals(existingUser.getUsername())) {

            if (userRepository.existsByUsernameAndUserIdNot(updateRequest.getUsername(), id))
                throw new IllegalArgumentException("Username already taken: " + updateRequest.getUsername());
            existingUser.setUsername(updateRequest.getUsername());
        }

        CredentialEntity credential = existingUser.getCredential();


        if (updateRequest.getActive() != null)
            existingUser.setActive(updateRequest.getActive());

        UserEntity savedUser = userRepository.save(existingUser);
        return userMapper.toUserProfileResponse(savedUser);
    }

    @Transactional
    public void updatePassword(Long id, PasswordUpdateRequest passwordRequest, Authentication authentication) {
        UserEntity user = findEntityById(id);
        CredentialEntity credential = user.getCredential();

        if (credential == null)
            throw new IllegalStateException("User has no credentials");

        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), credential.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");

        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword()))
            throw new IllegalArgumentException("New password and confirmation do not match");

        credential.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        credentialRepository.save(credential);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long id, ProfileUpdateRequest profileRequest) {
        UserEntity user = findEntityById(id);
        CredentialEntity credential = user.getCredential();

        if (credential != null) {
            if (profileRequest.getBiography() != null) {
                credential.setBiography(profileRequest.getBiography());
            }

            credentialRepository.save(credential);
        }

        return userMapper.toUserProfileResponse(user);
    }

    public UserProfileResponse getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        CredentialEntity credential = credentialRepository.findByEmailOrUsername(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + email));
        UserEntity user = credential.getUser();
        if (user == null) {
            throw new EntityNotFoundException("User entity not associated with credential for email: " + email);
        }

        UserStatsResponse stats = statisticService.getUserStatistics(user.getUserId());

        return userMapper.toUserProfileResponse(user, stats);
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

    public UserProfileResponse updateUserProfile(Long id, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getBiography() != null && user.getCredential() != null) {
            user.getCredential().setBiography(request.getBiography());
        }

        userRepository.save(user);

        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    public AuthResponse completeOAuthProfile(CompleteProfileRequest request, String authenticatedUserEmail) {

        String newUsername = request.username().trim();
        if (newUsername.length() < 3 || newUsername.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        CredentialEntity credential = credentialRepository.findByEmailIgnoreCase(authenticatedUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        UserEntity user = credential.getUser();

        boolean isIncomplete = credential.getRoles().stream()
                .anyMatch(role -> role.getRole() == Role.ROLE_INCOMPLETE_PROFILE);

        if (!isIncomplete) {
            throw new IllegalStateException("Profile is already complete.");
        }

        if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
            throw new IllegalArgumentException("Username already taken: " + newUsername);
        }

        user.setUsername(newUsername);
        userRepository.save(user);

        RoleEntity userRole = roleRepository.findByRole(Role.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("Role ROLE_USER not found."));

        credential.getRoles().clear();
        credential.getRoles().add(userRole);
        credentialRepository.save(credential);

        String newToken = jwtService.generateToken(credential);

        return authMapper.toAuthResponse(credential, newToken);
    }
}