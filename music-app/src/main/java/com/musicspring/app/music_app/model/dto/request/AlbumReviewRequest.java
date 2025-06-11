package com.musicspring.app.music_app.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Request payload for creating or updating an album review",
        requiredProperties = {"userId", "rating", "description"})
public class AlbumReviewRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user who created the review",
            example = "1")
    private Long userId;

    @NotNull(message = "Rating is required.")
    @DecimalMin(value = "0.5", message = "Rating must be at least 0.5")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    @Digits(integer = 1, fraction = 2, message = "Rating format is invalid.")
    @Schema(description = "Rating given to the album (between 0.5 and 5.0)",
            example = "4.5")
    private Double rating;

    @NotBlank(message = "Description is required.")
    @Size(max = 500)
    @Schema(description = "Textual description of the review",
            example = "Great album with amazing tracks!", maxLength = 500)
    private String description;
}