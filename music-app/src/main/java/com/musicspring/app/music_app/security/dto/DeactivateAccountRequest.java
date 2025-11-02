package com.musicspring.app.music_app.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Request payload to confirm account deactivation")
public record DeactivateAccountRequest(
        @NotBlank(message = "Password is required.")
        @Schema(description = "User's current password to confirm the deactivation",
                example = "password123")
        String password
) {
}
