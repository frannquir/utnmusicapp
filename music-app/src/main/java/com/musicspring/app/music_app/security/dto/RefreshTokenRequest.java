package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for refreshing authentication tokens",
        requiredProperties = {"refreshToken"})
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token is required.")
        @Schema(description = "The refresh token used to renew the authentication token",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {}