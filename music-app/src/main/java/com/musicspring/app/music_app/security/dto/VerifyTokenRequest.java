package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for verifying user account via email code.",
        requiredProperties = {"token"})
public record VerifyTokenRequest(
        @NotBlank(message = "The token is required.")
        @Schema(
                description = "The 6-character verification code sent via email.",
                example = "1A2B3C"
        )
        String token
) {}