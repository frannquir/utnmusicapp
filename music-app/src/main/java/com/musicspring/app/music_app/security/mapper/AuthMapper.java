package com.musicspring.app.music_app.security.mapper;

import com.musicspring.app.music_app.model.mapper.SecurityMapper;
import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    private final SecurityMapper securityMapper;

    public AuthMapper(SecurityMapper securityMapper) {
        this.securityMapper = securityMapper;
    }

    public AuthResponse toAuthResponse(CredentialEntity user, String token) {

        return AuthResponse.builder()
                .token(token)
                .refreshToken(user.getRefreshToken())
                .id(user.getUser().getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(securityMapper.toRoleNames(user))
                .permissions(securityMapper.toPermissionNames(user))
                .build();
    }
}
