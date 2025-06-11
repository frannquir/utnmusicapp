package com.musicspring.app.music_app.model.dto.response;

import com.musicspring.app.music_app.model.enums.CommentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CommentResponse {

    private Long commentId;

    private Long reviewId;

    private Long userId;

    private String text;

    private LocalDateTime createdAt;

    private CommentType commentType;

    private String username;

}
