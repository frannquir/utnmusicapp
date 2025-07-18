package com.musicspring.app.music_app.model.dto.response;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfileResponse {
    // Información básica del usuario
    private Long id;
    private String username;
    private String biography;
    private String profilePictureUrl;
    private String joinDate;
    
    // Estadísticas de reviews (campos originales)
    private Long totalAlbumReviews;
    private Long totalSongReviews;
    private Double averageRating;
    
    // Estadísticas adicionales de reviews
    private Long totalReviews;
    
    // Estadísticas de comentarios
    private Long totalComments;
    private Long albumComments;
    private Long songComments;
    
    // Estadísticas de reacciones dadas
    private Long totalReactions;
    private Long likesGiven;
    private Long lovesGiven;
    private Long wowsGiven;
    private Long dislikesGiven;
    
    // Estadísticas de reacciones recibidas
    private Long likesReceived;
    private Long lovesReceived;
    private Long wowsReceived;
    private Long dislikesReceived;
    
    // Actividad reciente
    private Long reviewsThisMonth;
    private Long commentsThisMonth;
    private Long reactionsThisMonth;
}