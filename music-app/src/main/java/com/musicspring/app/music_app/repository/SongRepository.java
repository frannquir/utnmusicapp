package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.SongEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<SongEntity, Long> {

    Optional<SongEntity> findBySpotifyId(String spotifyId);

    Page<SongEntity> findByNameContainingIgnoreCase(
            String name, String artistName, Pageable pageable);

    @Query("SELECT s " +
            "FROM SongEntity s " +
            "LEFT JOIN SongReviewEntity r ON s.songId = r.song.songId " +
            "GROUP BY s " +
            "ORDER BY COUNT(r) DESC")
    Page<SongEntity> findTopReviewedSongs(Pageable pageable);


}
