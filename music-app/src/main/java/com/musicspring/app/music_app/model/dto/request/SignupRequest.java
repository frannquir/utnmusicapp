package com.musicspring.app.music_app.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for signing up with email",
        requiredProperties = {"username", "email", "password"})
public class SignupRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Schema(description = "Username of the user", example = "user1234")
    private String username;

    @NotBlank(message = "Email is required")
    @Email
    @Schema(description = "Email of the user", example = "musiclover@gmail.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 120)
    @Schema(description = "Password of the user", example = "Password123")
    private String password;
}