package com.musicspring.app.music_app.model.dto.response;

import lombok.*;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String biography;
    private String joinDate;
    private Boolean active;
    private Boolean isBanned;
    private Set<String> roles;
    private Set<String> permissions;
    private UserStatsResponse userStats;
    private String profilePictureUrl;
}