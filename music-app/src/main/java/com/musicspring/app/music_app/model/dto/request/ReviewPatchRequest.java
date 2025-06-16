package com.musicspring.app.music_app.model.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Request used to update the content of an existing review")
public class ReviewPatchRequest {

    @Schema(description = "Updated content of the review", example = "I really love Travis Scott work!", maxLength = 500)
    @NotBlank(message = "Review text cannot be blank")
    @Size(min = 1, max = 500)
    private String description;

}
