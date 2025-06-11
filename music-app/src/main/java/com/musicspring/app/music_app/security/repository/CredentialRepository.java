package com.musicspring.app.music_app.security.repository;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, Long> {

    Optional<CredentialEntity> findByEmail(String email);

    @Query("SELECT c FROM CredentialEntity c JOIN c.user u WHERE u.username = :username")
    Optional<CredentialEntity> findByUsername(String username);

    @Query("SELECT c FROM CredentialEntity c JOIN c.user u WHERE c.email = :identifier OR u.username = :identifier")
    Optional<CredentialEntity> findByEmailOrUsername(String identifier);
}
