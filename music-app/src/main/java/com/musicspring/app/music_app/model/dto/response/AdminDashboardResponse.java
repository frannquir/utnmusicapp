package com.musicspring.app.music_app.model.dto.response;

import lombok.Builder;

@Builder
public record AdminDashboardResponse(
    // User stats
    Long totalUsers,
    Long activeUsers,
    Long bannedUsers,

    // Content stats
    Long totalReviews,
    Long totalComments,
    Long totalReactions,

    // Activity breakdown
    Long albumReviews,
    Long songReviews,
    Long likesCount,
    Long lovesCount,
    Long wowsCount,
    Long dislikesCount
) {}
