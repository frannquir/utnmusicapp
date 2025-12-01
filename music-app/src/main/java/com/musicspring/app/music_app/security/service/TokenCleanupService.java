package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.security.repository.EmailVerificatorTokenRepository;
import com.musicspring.app.music_app.security.repository.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    private final EmailVerificatorTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository passwordTokenRepository;

    public TokenCleanupService(EmailVerificatorTokenRepository emailTokenRepository,
                               PasswordResetTokenRepository passwordTokenRepository) {
        this.emailTokenRepository = emailTokenRepository;
        this.passwordTokenRepository = passwordTokenRepository;
    }
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        emailTokenRepository.deleteByExpirationBefore(now);
        passwordTokenRepository.deleteByExpirationBefore(now);
    }
}