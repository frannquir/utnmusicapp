package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.entity.EmailVerificatorTokenEntity;
import com.musicspring.app.music_app.security.repository.EmailVerificatorTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.security.SecureRandom;

@Service
public class EmailVerificatorService {

    private final JavaMailSender mailSender;
    private final EmailVerificatorTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    @Autowired
    public EmailVerificatorService(JavaMailSender mailSender,
                                   EmailVerificatorTokenRepository tokenRepository,
                                   UserRepository userRepository) {
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public void sendVerificationEmail(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("The provided user is NULL");
        }
        if (user.getCredential() == null) {
            throw new IllegalStateException("Unable to send email: Credentials not found for user with ID: .");
        }
        if (tokenRepository == null) {
            throw new IllegalStateException("Spring configuration failed (tokenRepository is NULL).");
        }
        if (mailSender == null) {
            throw new IllegalStateException("Spring configuration failed (MailSender is NULL).");
        }

        String token = generateRandomCode(CODE_LENGTH);

        EmailVerificatorTokenEntity emailToken = new EmailVerificatorTokenEntity();
        emailToken.setToken(token);
        emailToken.setUser(user);
        emailToken.setExpiration(LocalDateTime.now().plusMinutes(10));

        tokenRepository.save(emailToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail != null ? fromEmail : "noreply@echoed.com");
        message.setTo(user.getCredential().getEmail());
        message.setSubject("Email verification - Echoed");
        message.setText("Hi " + user.getUsername()
                + "!\n\nUse this code to verify your account: \n\n"
                + token + "\n\nThe code expires in 10 minutes.");

        mailSender.send(message);
        System.out.println("Email successfully sent to: " + user.getCredential().getEmail());
    }

    public void verifyToken(String token) {
        EmailVerificatorTokenEntity emailVerificatorTokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (emailVerificatorTokenEntity.getExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        UserEntity user = emailVerificatorTokenEntity.getUser();
        user.setActive(true);
        userRepository.save(user);

        tokenRepository.delete(emailVerificatorTokenEntity);
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