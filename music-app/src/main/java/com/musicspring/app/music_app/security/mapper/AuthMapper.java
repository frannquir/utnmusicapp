package com.musicspring.app.music_app.security.mapper;

import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(CredentialEntity user, String token) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(user.getRefreshToken())
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
