package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    //Metodos posibles a implementar si queremos trabajar con reseñas sin diferenciar el tipo (album o cancion)

    @Query("SELECT r FROM ReviewEntity r WHERE r.reviewId = :reviewId AND r.active = true")
    Optional<ReviewEntity> findById(@Param("reviewId") Long reviewId);

    Page<ReviewEntity> findByUser_UserId(Long userId, Pageable pageable);

    Page<ReviewEntity> findByActiveTrue(Pageable pageable);

    Page<ReviewEntity> findByRatingGreaterThanEqual(Double rating, Pageable pageable);

    Page<ReviewEntity> findByRatingBetween(Double minRating, Double maxRating, Pageable pageable);

    Page<ReviewEntity> findByUser_UserIdAndActiveTrueAndRatingGreaterThanEqual(Long userId, Double rating, Pageable pageable);
}
