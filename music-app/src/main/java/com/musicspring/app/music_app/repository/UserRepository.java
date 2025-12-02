package com.musicspring.app.music_app.repository;

import com.musicspring.app.music_app.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.active = true")
    Optional<UserEntity> findByUsername(@Param("username") String username);

    Boolean existsByUsername(String username);

    Boolean existsByUsernameIgnoreCase(String username);

    Boolean existsByUsernameAndUserIdNot(String username, Long userId);

    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) AND u.active = true")
    Page<UserEntity> findByUsernameContainingIgnoreCase(@Param("username") String username, Pageable pageable);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.credential c WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<UserEntity> findByUsernameOrEmailContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    @Query(value = """
    SELECT AVG(all_reviews.rating) FROM (
        SELECT r.rating FROM reviews r
        INNER JOIN album_reviews ar ON r.review_id = ar.review_id
        WHERE r.user_id = :userId AND r.active = true
        UNION ALL
        SELECT r.rating FROM reviews r
        INNER JOIN song_reviews sr ON r.review_id = sr.review_id
        WHERE r.user_id = :userId AND r.active = true
    ) all_reviews
    """, nativeQuery = true)
    Double calculateUserAverageRating(@Param("userId") Long userId);

    List<UserEntity> findAll();

    @Query("SELECT u FROM UserEntity u WHERE u.active = true")
    List<UserEntity> findAllActiveUsers();

    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId AND u.active = false")
    Optional<UserEntity> findByIdAndActiveFalse(@Param("userId") Long userId);

    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId AND u.active = true")
    Optional<UserEntity> findByIdAndActiveTrue(@Param("userId") Long userId);
}
