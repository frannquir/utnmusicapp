package com.musicspring.app.music_app.security.repository;

import com.musicspring.app.music_app.security.entity.EmailVerificatorTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificatorTokenRepository extends JpaRepository<EmailVerificatorTokenEntity, Long> {

    Optional<EmailVerificatorTokenEntity> findByToken(String token);
}
