package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.AlbumEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {
    Optional<AlbumEntity> findBySpotifyId(String spotifyId);

    List<AlbumEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT a " +
            "FROM AlbumEntity a " +
            "LEFT JOIN AlbumReviewEntity r ON a.albumId = r.album.albumId " +
            "GROUP BY a " +
            "HAVING COUNT(r) > 0 " +
            "ORDER BY COUNT(r) DESC")
    Page<AlbumEntity> findTopReviewedAlbums(Pageable pageable);


}
