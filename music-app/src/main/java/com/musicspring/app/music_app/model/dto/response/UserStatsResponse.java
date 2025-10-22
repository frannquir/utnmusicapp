package com.musicspring.app.music_app.model.dto.response;

import lombok.Builder;

@Builder
public record UserStatsResponse(Long totalAlbumReviews, Long totalSongReviews, Long totalReviews, Double averageRating,
                                Long totalComments, Long albumComments, Long songComments,
                                Long totalReactions, Long likesGiven, Long lovesGiven, Long wowsGiven, Long dislikesGiven,
                                Long likesReceived, Long lovesReceived, Long wowsReceived, Long dislikesReceived,
                                Long reviewsThisMonth, Long commentsThisMonth, Long reactionsThisMonth) {
}
