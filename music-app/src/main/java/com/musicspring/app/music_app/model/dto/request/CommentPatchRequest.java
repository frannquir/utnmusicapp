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

@Schema(description = "Request used to update the content of an existing comment")
public class CommentPatchRequest {

    @Schema(description = "Updated content of the comment", example = "I totally agree with this review", maxLength = 500)
    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 1, max = 500)
    private String text;

}
