package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.ArtistEntity;
import com.musicspring.app.music_app.model.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<ArtistEntity, Long> {

    Page<ArtistEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<ArtistEntity> findBySpotifyId(String spotifyId);

    @Query("SELECT a " +
            "FROM ArtistEntity a " +
            "LEFT JOIN AlbumEntity al ON al.artist = a " +
            "LEFT JOIN SongEntity s ON s.album = al " +
            "LEFT JOIN SongReviewEntity sr ON s.songId = sr.song.songId AND sr.active = true " +
            "LEFT JOIN ReactionEntity r1 ON sr.reviewId = r1.review.reviewId " +
            "LEFT JOIN AlbumReviewEntity ar ON al.albumId = ar.album.albumId AND ar.active = true " +
            "LEFT JOIN ReactionEntity r2 ON ar.reviewId = r2.review.reviewId " +
            "WHERE (r1.reactionType = :reactionType OR r2.reactionType = :reactionType) " +
            "GROUP BY a " +
            "HAVING COUNT(r1) > 0 OR COUNT(r2) > 0 " +
            "ORDER BY COUNT(r1) + COUNT(r2) DESC")
    Page<ArtistEntity> findTopArtistsByReactionType(@Param("reactionType") ReactionType reactionType, Pageable pageable);

    @Query("SELECT a " +
            "FROM ArtistEntity a " +
            "LEFT JOIN a.albums al " +
            "LEFT JOIN al.songs s " +
            "LEFT JOIN SongReviewEntity sr ON s.songId = sr.song.songId " +
            "LEFT JOIN AlbumReviewEntity ar ON al.albumId = ar.album.albumId " +
            "WHERE (ar.active = true OR sr.active = true) " +
            "GROUP BY a " +
            "ORDER BY (COUNT(sr.reviewId) + COUNT(ar.reviewId)) DESC")
    Page<ArtistEntity> findMostReviewedArtists(Pageable pageable);

}