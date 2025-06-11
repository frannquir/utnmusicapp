package com.musicspring.app.music_app.model.dto.response;

import com.musicspring.app.music_app.model.enums.ReactedType;
import com.musicspring.app.music_app.model.enums.ReactionType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ReactionResponse {

    private Long reactionId;

    private Long userId;

    private String username;

    private ReactionType reactionType;

    private ReactedType reactedType;

    private Long reactedId;

}
