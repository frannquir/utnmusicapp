package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.security.dto.ForgotPasswordRequest;
import com.musicspring.app.music_app.security.dto.ResetPasswordRequest;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.PasswordResetTokenEntity;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.PasswordResetTokenRepository;
import com.musicspring.app.music_app.model.enums.BrandColors;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PasswordResetService extends AbstractEmailService {

    private final CredentialRepository credentialRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetService(CredentialRepository credentialRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        super(mailSender);
        this.credentialRepository = credentialRepository;
        this.tokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        CredentialEntity credential = credentialRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        UserEntity user = credential.getUser();


        java.util.Optional<PasswordResetTokenEntity> existingToken = tokenRepository.findByUser(user);

        if (existingToken.isPresent()) {
            PasswordResetTokenEntity token = existingToken.get();
            if (token.getExpiration().isAfter(LocalDateTime.now().plusMinutes(9))) {
                throw new IllegalStateException("Please wait a minute before requesting a new code.");
            }
        }

        String token = generateRandomCode(CODE_LENGTH);

        PasswordResetTokenEntity resetToken = existingToken.orElse(new PasswordResetTokenEntity());

        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(10));

        tokenRepository.save(resetToken);

        String htmlContent = buildEmailContent(user.getUsername(), token);
        sendHtmlEmail(credential.getEmail(), "Reset your Echoed password", htmlContent);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        UserEntity user = resetToken.getUser();
        CredentialEntity credential = user.getCredential();

        credential.setPassword(passwordEncoder.encode(request.newPassword()));
        credentialRepository.save(credential);

        tokenRepository.delete(resetToken);
    }

    private String buildEmailContent(String username, String token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Password</title>
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
                                        <h2 style="font-family: 'Poppins', sans-serif; color: %s; margin-top: 0;">Hi %s,</h2>
                                        <p style="color: %s; font-size: 16px; line-height: 1.6;">
                                            We received a request to reset your password. Use the code below to securely update your credentials.
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
                                            This code is valid for <strong>10 minutes</strong>. If you did not request this, please ignore this email.
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