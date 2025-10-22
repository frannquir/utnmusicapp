package com.musicspring.app.music_app.security.dto;

import com.musicspring.app.music_app.security.entity.PermitEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@Jacksonized
@Builder
public record AuthResponse(String token,
                           String refreshToken,
                           Long id,
                           String username,
                           String email,
                           Set<String> roles,
                           Set<String> permissions) { }
