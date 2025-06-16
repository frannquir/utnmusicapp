package com.musicspring.app.music_app.security.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record AuthResponse(String token,
                           String refreshToken,
                           Long id,
                           String username,
                           String email) { }
