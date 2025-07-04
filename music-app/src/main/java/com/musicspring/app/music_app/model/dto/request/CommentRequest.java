package com.musicspring.app.music_app.model.dto.request;


import com.musicspring.app.music_app.model.enums.CommentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Request object for creating a comment",
        requiredProperties = {"userId", "text", "commentType"})
public class CommentRequest {

    @Schema(description = "ID of the user making the comment", example = "1")
    @NotNull(message = "User ID is required")
    private Long userId;

    @Schema(description = "Text content of the comment", example = "Great review, thanks for sharing!")
    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 1, max = 500)
    private String text;
}