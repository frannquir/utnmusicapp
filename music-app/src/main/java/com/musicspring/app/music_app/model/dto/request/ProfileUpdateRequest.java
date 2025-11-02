package com.musicspring.app.music_app.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Request object for updating the user's profile")
public class ProfileUpdateRequest {

    @Size(max = 500, message = "Biography must be at most 500 characters.")
    @Schema(description = "Short biography or personal description of the user.", example = "Amateur guitarist, vinyl collector and music lover!")
    private String biography;
}