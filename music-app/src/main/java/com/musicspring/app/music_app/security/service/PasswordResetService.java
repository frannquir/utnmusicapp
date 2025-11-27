package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.security.dto.ForgotPasswordRequest;
import com.musicspring.app.music_app.security.dto.ResetPasswordRequest;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.PasswordResetTokenEntity;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.PasswordResetTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private final CredentialRepository credentialRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value( "${spring.mail.username}")
    private String fromEmail;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    @Autowired
    public PasswordResetService(CredentialRepository credentialRepository, PasswordResetTokenRepository passwordResetTokenRepository, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.tokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        CredentialEntity credential = credentialRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        UserEntity user = credential.getUser();

        String token = generateRandomCode(CODE_LENGTH);

        PasswordResetTokenEntity resetToken = tokenRepository.findByUser(user)
                        .orElse(new PasswordResetTokenEntity());
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(10));

        tokenRepository.save(resetToken);

        sendResetEmail(credential.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if(resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        UserEntity user = resetToken.getUser();
        CredentialEntity credential = user.getCredential();

        credential.setPassword(passwordEncoder.encode(request.newPassword()));
        credentialRepository.save(credential);

        tokenRepository.delete(resetToken);
    }

    private void sendResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail != null ? fromEmail : "noreply@echoed.com");
        message.setTo(toEmail);
        message.setSubject("Password reset - Echoed");
        message.setText(
                "Hi,\n\n"
                        + "We received a request to reset your password.\n"
                        + "Your recovery code is:\n\n"
                        + token + "\n\n"
                        + "If you didn’t request this, please ignore this message.\n"
                        + "This code expires in 10 minutes."
        );


        mailSender.send(message);
    }

    private String generateRandomCode(int length){
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }





}
