package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for completing a user profile (e.g., setting username after OAuth)",
        requiredProperties = {"username"})
public record CompleteProfileRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Schema(description = "The unique username chosen by the user",
                example = "echoUser99")
        String username
) {
}
