package com.musicspring.app.music_app.security;

import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.service.JwtService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** Shared test fixtures: a real JwtService plus a sample credential. */
public final class TestTokens {

    public static final long ACCESS_EXPIRATION = 1_800_000L;
    public static final long REFRESH_EXPIRATION = 2_592_000_000L;

    private TestTokens() {
    }

    public static JwtService jwtService() {
        JwtService jwtService = new JwtService();
        // base64 of "test-secret-test-secret-test-secret"
        ReflectionTestUtils.setField(jwtService, "jwtSecretKey", "dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ=");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_EXPIRATION);
        return jwtService;
    }

    /** Refresh token with a custom lifetime; negative means already expired. */
    public static String refreshToken(JwtService jwtService, CredentialEntity credential, long expirationMs) {
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", expirationMs);
        try {
            return jwtService.generateRefreshToken(credential);
        } finally {
            ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_EXPIRATION);
        }
    }

    public static CredentialEntity credential(AuthProvider provider) {
        UserEntity user = UserEntity.builder()
                .userId(1L)
                .username("fran")
                .active(true)
                .isBanned(false)
                .build();
        RoleEntity role = RoleEntity.builder()
                .role(Role.ROLE_USER)
                .permits(new ArrayList<>())
                .build();
        return CredentialEntity.builder()
                .id(1L)
                .email("fran@echoed.com")
                .password("encoded")
                .provider(provider)
                .user(user)
                .roles(new HashSet<>(Set.of(role)))
                .build();
    }
}
