package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.AlbumEntity;
import com.musicspring.app.music_app.model.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {
    Optional<AlbumEntity> findBySpotifyId(String spotifyId);

    List<AlbumEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT a " +
            "FROM AlbumEntity a " +
            "LEFT JOIN AlbumReviewEntity ar ON ar.album = a " +
            "WHERE ar.active = true " +
            "GROUP BY a " +
            "HAVING COUNT(ar) > 0 " +
            "ORDER BY COUNT(ar) DESC")
    Page<AlbumEntity> findTopReviewedAlbums(Pageable pageable);

    @Query("SELECT al " +
            "FROM AlbumEntity al " +
            "LEFT JOIN AlbumReviewEntity ar ON ar.album.albumId = al.albumId AND ar.active = true " +
            "LEFT JOIN ReactionEntity r ON ar.reviewId = r.review.reviewId " +
            "WHERE r.reactionType = :reactionType " +
            "GROUP BY al " +
            "HAVING COUNT(r.id) > 0 " +
            "ORDER BY COUNT(r.id) DESC")
    Page<AlbumEntity> findMostReactedAlbums(@Param("reactionType") ReactionType reactionType, Pageable pageable);
    
}
