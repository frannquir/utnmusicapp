package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.AlbumReviewEntity;
import com.musicspring.app.music_app.model.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumReviewRepository extends JpaRepository<AlbumReviewEntity,Long> {

    @Query("SELECT ar FROM AlbumReviewEntity ar WHERE ar.active = true")
    Page<AlbumReviewEntity> findAll(Pageable pageable);

    @Query("SELECT ar FROM AlbumReviewEntity ar WHERE ar.reviewId = :reviewId AND ar.active = true")
    Optional<AlbumReviewEntity> findById(@Param("reviewId") Long reviewId);

    @Query("SELECT ar FROM AlbumReviewEntity ar WHERE ar.album.albumId = :albumId AND ar.active = true")
    Page<AlbumReviewEntity> findByAlbum_AlbumId(Long albumId, Pageable pageable);

    @Query("SELECT ar FROM AlbumReviewEntity ar WHERE ar.user.userId = :userId AND ar.active = true")
    Page<AlbumReviewEntity> findByUser_UserId(Long userId,Pageable pageable);

    @Query("SELECT ar FROM AlbumReviewEntity ar WHERE ar.user.userId = :userId AND ar.active = false")
    Page<AlbumReviewEntity> findByUser_UserIdAndActiveFalse(Long userId,Pageable pageable);

    Page<AlbumReviewEntity> findByUser_UserIdAndActiveTrue(Long userId, Pageable pageable);

    @Query("SELECT ar FROM AlbumReviewEntity ar WHERE ar.album.spotifyId = :spotifyId AND ar.active = true")
    Page<AlbumReviewEntity> findByAlbum_SpotifyId(@Param("spotifyId") String spotifyId, Pageable pageable);

    long countByUser_UserId(Long userId);

    @Query("SELECT COUNT(ar) FROM AlbumReviewEntity ar WHERE ar.user.userId = :userId AND ar.active = true")
    Long countAlbumReviewsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE AlbumReviewEntity ar SET ar.active = false WHERE ar.user.userId = :userId")
    void deactivateByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE AlbumReviewEntity ar SET ar.active = true WHERE ar.user.userId = :userId AND ar.active = false")
    void reactivateByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ar) FROM AlbumReviewEntity ar WHERE ar.user.userId = :userId AND ar.active = true")
    Long countByUserUserId(@Param("userId") Long userId);

}
