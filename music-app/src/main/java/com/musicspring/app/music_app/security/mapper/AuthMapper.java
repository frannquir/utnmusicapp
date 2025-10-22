package com.musicspring.app.music_app.security.mapper;

import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthMapper {

    public AuthResponse toAuthResponse(CredentialEntity user, String token) {

        Set<String> roleNames = user.getRoles().stream()
                .map(roleEntity -> roleEntity.getRole().name())
                .collect(Collectors.toSet());

        Set<String> permissionNames = user.getRoles().stream()
                .flatMap(roleEntity -> roleEntity.getPermits().stream())
                .map(permitEntity -> permitEntity.getPermit().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(user.getRefreshToken())
                .id(user.getUser().getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .permissions(permissionNames)
                .build();
    }
}
