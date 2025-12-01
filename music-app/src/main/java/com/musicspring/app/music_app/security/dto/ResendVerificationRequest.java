package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to resend the verification code")
public record ResendVerificationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "user@example.com")
        String email
) {}