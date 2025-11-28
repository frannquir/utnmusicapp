package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to set the new password")
public record ResetPasswordRequest(
        @NotBlank(message = "Token is required")
        @Schema(description = "6-digit code received by email", example = "X9B2N1")
        String token,

        @NotBlank(message = "The new password is required")
        @Size(min = 8, message = "The password must be at least 8 characters long")
        @Schema(description = "The new password the user wants to set", example = "NewPass123!")
        String newPassword
) {}
