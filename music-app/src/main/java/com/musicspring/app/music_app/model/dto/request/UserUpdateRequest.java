package com.musicspring.app.music_app.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for updating the user's profile",
        requiredProperties = {"username"})
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "New username for the user", example = "new_username")
    private String username;

    @Schema(description = "URL of the user's profile picture", example = "https://example.com/profile.jpg")
    @Size(max = 255, message = "Image URL must be at most 255 characters.")
    private String profilePictureUrl;

    @Schema(description = "User's active status", example = "true")
    private Boolean active;
}