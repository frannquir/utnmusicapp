package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for user authentication",
        requiredProperties = {"emailOrUsername", "password"})
public record AuthRequest(
        @NotBlank(message = "Email is required.")
        @Schema(description = "Email of the user attempting to authenticate",
                example = "user@gmail.com")
        String emailOrUsername,

        @NotBlank(message = "Password is required.")
        @Schema(description = "Password of the user attempting to authenticate",
                example = "password123")
        String password)
{}
