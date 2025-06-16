package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.SongReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongReviewRepository extends JpaRepository<SongReviewEntity,Long> {

    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.active = true")
    Page<SongReviewEntity> findAll(Pageable pageable);

    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.reviewId = :reviewId AND sr.active = true")
    Optional<SongReviewEntity> findById(@Param("reviewId") Long reviewId);

    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.song.songId = :songId AND sr.active = true")
    Page<SongReviewEntity> findBySong_Id(@Param("songId") Long songId, Pageable pageable);

    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.user.userId = :userId AND sr.active = true")
    Page<SongReviewEntity> findByUser_UserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.song.spotifyId = :spotifyId AND sr.active = true")
    Page<SongReviewEntity> findBySong_SpotifyId(@Param("spotifyId") String spotifyId, Pageable pageable);

    long countByUser_UserId(Long userId);
}