package com.musicspring.app.music_app.model.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Request used to update the rating and description of an existing review")
public class ReviewUpdateRequest {

    @NotNull(message = "Rating is required.")
    @DecimalMin(value = "0.5", message = "Rating must be at least 0.5")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    @Digits(integer = 1, fraction = 2, message = "Rating format is invalid.")
    @Schema(description = "Updated rating for the review (between 0.5 and 5.0)", example = "4.5")
    private Double rating;

    @NotBlank(message = "Description is required.")
    @Size(min = 1, max = 500, message = "Description must be between 1 and 500 characters.")
    @Schema(description = "Updated content of the review", example = "I really love Travis Scott work!", maxLength = 500)
    private String description;

}
