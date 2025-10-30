package com.musicspring.app.music_app.security.repository;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, Long> {

    Optional<CredentialEntity> findByEmail(String email);

    Optional<CredentialEntity> findByEmailIgnoreCase(String email);

    @Query("SELECT c FROM CredentialEntity c JOIN c.user u WHERE u.username = :username")
    Optional<CredentialEntity> findByUsername(String username);

    @Query("SELECT c FROM CredentialEntity c JOIN c.user u WHERE LOWER(c.email) = LOWER(:identifier) OR LOWER(u.username) = LOWER(:identifier)")
    Optional<CredentialEntity> findByEmailOrUsername(@Param("identifier") String identifier);
}
