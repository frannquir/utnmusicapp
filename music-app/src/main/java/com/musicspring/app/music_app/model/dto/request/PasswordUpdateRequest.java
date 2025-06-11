package com.musicspring.app.music_app.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for updating the user's password",
        requiredProperties = {"currentPassword", "newPassword", "confirmPassword"})
public class PasswordUpdateRequest {

    @NotBlank(message = "Current password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Current password of the user", example = "Password123")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "New password of the user", example = "NewPassword123")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Password confirmation of the user", example = "NewPassword123")
    private String confirmPassword;
}