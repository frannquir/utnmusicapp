package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.CommentEntity;
import com.musicspring.app.music_app.model.entity.SongReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.user.userId = :userId AND sr.active = false")
    Page<SongReviewEntity> findByUser_UserIdAndActiveFalse(@Param("userId") Long userId, Pageable pageable);

    Page<SongReviewEntity> findByUser_UserIdAndActiveTrue(Long userId, Pageable pageable);

    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.song.spotifyId = :spotifyId AND sr.active = true")
    Page<SongReviewEntity> findBySong_SpotifyId(@Param("spotifyId") String spotifyId, Pageable pageable);

    long countByUser_UserId(Long userId);

    @Query("SELECT COUNT(sr) FROM SongReviewEntity sr WHERE sr.user.userId = :userId AND sr.active = true")
    Long countSongReviewsByUserId(@Param("userId") Long userId);


    @Modifying
    @Query("UPDATE SongReviewEntity sr SET sr.active = false WHERE sr.user.userId = :userId")
    void deactivateByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE SongReviewEntity sr SET sr.active = true WHERE sr.user.userId = :userId AND sr.active = false")
    void reactivateByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(sr) FROM SongReviewEntity sr WHERE sr.user.userId = :userId AND sr.active = true")
    Long countByUserUserId(@Param("userId") Long userId);

    @Query("SELECT sr FROM SongReviewEntity sr WHERE sr.user.userId = :userId AND sr.song.songId = :songId AND sr.active = true")
    Optional<SongReviewEntity> findByUserUserIdAndSongSongIdAndActiveTrue(@Param("userId") Long userId, @Param("songId") Long songId);
}