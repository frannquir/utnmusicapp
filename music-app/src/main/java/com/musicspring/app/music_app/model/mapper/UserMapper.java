package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.SignupRequest;
import com.musicspring.app.music_app.model.dto.response.UserProfileResponse;
import com.musicspring.app.music_app.model.dto.response.UserResponse;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.security.dto.AuthRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapper {
    public UserResponse toResponse(UserEntity user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .roles(user.getCredential() != null ? user.getCredential().getRoles() : Set.of())
                .profilePictureUrl(user.getCredential() != null ? user.getCredential().getProfilePictureUrl() : null)
                .biography(user.getCredential() != null ? user.getCredential().getBiography() : null)
                .build();
    }
    public UserEntity toUserEntity(AuthRequest authRequest) {
        return UserEntity.builder()
                .username(authRequest.emailOrUsername())
                .active(true)
                .build();
    }
    public UserEntity toUserEntity (SignupRequest signupRequest) {
        return UserEntity.builder()
                .username(signupRequest.getUsername())
                .active(true)
                .build();
    }
    public UserProfileResponse toUserProfile (UserEntity user) {
        return UserProfileResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .biography(user.getCredential() != null ? user.getCredential().getBiography() : null)
                .profilePictureUrl(user.getCredential() != null ? user.getCredential().getProfilePictureUrl() : null)
                .build();
    }

    public UserProfileResponse toUserProfileWithStats(UserEntity user, 
                                                      Long totalAlbumReviews, Long totalSongReviews, Long totalReviews, Double averageRating,
                                                      Long totalComments, Long albumComments, Long songComments,
                                                      Long totalReactions, Long likesGiven, Long lovesGiven, Long wowsGiven, Long dislikesGiven,
                                                      Long likesReceived, Long lovesReceived, Long wowsReceived, Long dislikesReceived,
                                                      Long reviewsThisMonth, Long commentsThisMonth, Long reactionsThisMonth) {
        return UserProfileResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .biography(user.getCredential() != null ? user.getCredential().getBiography() : null)
                .profilePictureUrl(user.getCredential() != null ? user.getCredential().getProfilePictureUrl() : null)
                .joinDate(user.getCreatedAt().toLocalDate().toString())
                
                .totalAlbumReviews(totalAlbumReviews)
                .totalSongReviews(totalSongReviews)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                
                .totalComments(totalComments)
                .albumComments(albumComments)
                .songComments(songComments)
                
                .totalReactions(totalReactions)
                .likesGiven(likesGiven)
                .lovesGiven(lovesGiven)
                .wowsGiven(wowsGiven)
                .dislikesGiven(dislikesGiven)
                
                .likesReceived(likesReceived)
                .lovesReceived(lovesReceived)
                .wowsReceived(wowsReceived)
                .dislikesReceived(dislikesReceived)

                .reviewsThisMonth(reviewsThisMonth)
                .commentsThisMonth(commentsThisMonth)
                .reactionsThisMonth(reactionsThisMonth)
                
                .build();
    }

}
