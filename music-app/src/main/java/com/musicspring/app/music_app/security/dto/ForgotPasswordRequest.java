package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to start the password recovery process")
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        @Schema(description = "Email of the user who forgot their password", example = "user@example.com")
        String email
) {}