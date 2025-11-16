package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.SignupRequest;
import com.musicspring.app.music_app.model.dto.response.UserProfileResponse;
import com.musicspring.app.music_app.model.dto.response.UserStatsResponse;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.security.dto.AuthRequest;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapper {

    private final SecurityMapper securityMapper;

    public UserMapper(SecurityMapper securityMapper) {
        this.securityMapper = securityMapper;
    }

    public UserEntity toUserEntity(AuthRequest authRequest) {
        return UserEntity.builder()
                .username(authRequest.emailOrUsername())
                .active(true)
                .isBanned(false)
                .build();
    }
    public UserEntity toUserEntity (SignupRequest signupRequest) {
        return UserEntity.builder()
                .username(signupRequest.getUsername())
                .active(true)
                .isBanned(false)
                .build();
    }
    public UserProfileResponse toUserProfileResponse(UserEntity user, UserStatsResponse userStats) {
        CredentialEntity credential = user.getCredential();

        Set<String> roleNames = securityMapper.toRoleNames(credential);
        Set<String> permissionNames = securityMapper.toPermissionNames(credential);

        return UserProfileResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .joinDate(user.getCreatedAt().toLocalDate().toString())
                .biography(credential != null ? credential.getBiography() : null)
                .profilePictureUrl(credential != null ? credential.getProfilePictureUrl() : null)
                .roles(roleNames)
                .permissions(permissionNames)

                .userStats(userStats)

                .build();
    }

    public UserProfileResponse toUserProfileResponse(UserEntity user) {
        return toUserProfileResponse(user, null);
    }

}
