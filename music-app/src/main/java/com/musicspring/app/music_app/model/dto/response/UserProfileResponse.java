package com.musicspring.app.music_app.model.dto.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfileResponse {
    private Long id;
    private String username;
    private String biography;
    private String profilePictureUrl;
    private String joinDate;

    private Long totalAlbumReviews;
    private Long totalSongReviews;
    private Double averageRating;

    private Long totalReviews;

    private Long totalComments;
    private Long albumComments;
    private Long songComments;

    private Long totalReactions;
    private Long likesGiven;
    private Long lovesGiven;
    private Long wowsGiven;
    private Long dislikesGiven;

    private Long likesReceived;
    private Long lovesReceived;
    private Long wowsReceived;
    private Long dislikesReceived;
    
    private Long reviewsThisMonth;
    private Long commentsThisMonth;
    private Long reactionsThisMonth;
}