package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.entity.EmailVerificatorTokenEntity;
import com.musicspring.app.music_app.security.repository.EmailVerificatorTokenRepository;
import com.musicspring.app.music_app.model.enums.BrandColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class EmailVerificatorService extends AbstractEmailService {

    private final EmailVerificatorTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public EmailVerificatorService(JavaMailSender mailSender,
                                   EmailVerificatorTokenRepository tokenRepository,
                                   UserRepository userRepository) {
        super(mailSender);
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public void sendVerificationEmail(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("User is NULL");
        if (user.getCredential() == null) throw new IllegalStateException("Credentials not found for user ID: " + user.getUserId());

        String token = generateRandomCode(CODE_LENGTH);

        EmailVerificatorTokenEntity emailToken = tokenRepository.findByUser(user)
                .orElse(new EmailVerificatorTokenEntity());

        if (emailToken.getId() == null) {
            emailToken.setUser(user);
        }
        emailToken.setToken(token);
        emailToken.setExpiration(LocalDateTime.now().plusMinutes(10));

        tokenRepository.save(emailToken);

        String htmlContent = buildEmailContent(user.getUsername(), token);
        sendHtmlEmail(user.getCredential().getEmail(), "Email verification - Echoed", htmlContent);

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

    private String buildEmailContent(String username, String token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verify Your Account</title>
                <link href="https://fonts.googleapis.com/css2?family=Lato:wght@400;700&family=Poppins:wght@700&display=swap" rel="stylesheet">
            </head>
            <body style="font-family: 'Lato', Arial, sans-serif; background-color: %s; margin: 0; padding: 0; color: %s;">
                <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td align="center" style="padding: 40px 0;">
                            <table role="presentation" width="600" border="0" cellspacing="0" cellpadding="0" style="background-color: %s; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); overflow: hidden;">
                                <tr>
                                    <td align="center" style="background-color: %s; padding: 30px;">
                                        <h1 style="font-family: 'Poppins', sans-serif; color: %s; margin: 0; font-size: 28px; letter-spacing: 1px; text-transform: uppercase;">ECHOED</h1>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="font-family: 'Poppins', sans-serif; color: %s; margin-top: 0;">Hi %s!</h2>
                                        <p style="color: %s; font-size: 16px; line-height: 1.6;">
                                            Thanks for signing up with Echoed. To complete your registration and verify your account, please use the verification code below:
                                        </p>
                                        <table role="presentation" border="0" cellspacing="0" cellpadding="0" width="100%%">
                                            <tr>
                                                <td align="center" style="padding: 30px 0;">
                                                    <div style="border: 2px dashed %s; padding: 20px; border-radius: 8px; display: inline-block; background-color: rgba(175, 100, 45, 0.05);">
                                                        <span style="font-size: 32px; font-weight: bold; color: %s; letter-spacing: 5px; font-family: 'Courier New', monospace;">%s</span>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                        <p style="color: %s; font-size: 14px;">
                                            This code will expire in <strong>10 minutes</strong>. If you did not request this verification, please ignore this email.
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="center" style="background-color: %s; padding: 20px; border-top: 1px solid #d7ccc8;">
                                        <p style="color: %s; font-size: 12px; margin: 0;">
                                            &copy; 2025 Echoed. All rights reserved.<br>
                                            Your voice in music.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(
                BrandColors.BACKGROUND_MAIN, BrandColors.TEXT_MAIN, BrandColors.BACKGROUND_CARD,
                BrandColors.PRIMARY, BrandColors.TEXT_ON_PRIMARY, BrandColors.PRIMARY,
                username, BrandColors.TEXT_MAIN, BrandColors.PRIMARY, BrandColors.PRIMARY,
                token, BrandColors.TEXT_MAIN, BrandColors.BACKGROUND_CARD, BrandColors.TEXT_SECONDARY
        );
    }
}