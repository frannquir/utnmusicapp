package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.model.mapper.CredentialMapper;
import com.musicspring.app.music_app.model.mapper.SecurityMapper;
import com.musicspring.app.music_app.model.mapper.UserMapper;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.TestTokens;
import com.musicspring.app.music_app.security.dto.AuthRequest;
import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.mapper.AuthMapper;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.EmailVerificatorTokenRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private CredentialRepository credentialRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private CredentialMapper credentialMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleRepository roleRepository;
    @Mock private EmailVerificatorService emailVerificatorService;
    @Mock private EmailVerificatorTokenRepository emailVerificatorTokenRepository;

    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = TestTokens.jwtService();
        authService = new AuthService(credentialRepository, authenticationManager, jwtService, userRepository,
                userMapper, credentialMapper, passwordEncoder, roleRepository,
                new AuthMapper(new SecurityMapper()), emailVerificatorService, emailVerificatorTokenRepository);
    }

    @Test
    void passwordSignInIssuesAndStoresNewRefreshToken() {
        CredentialEntity credential = TestTokens.credential(AuthProvider.LOCAL);
        String oldToken = TestTokens.refreshToken(jwtService, credential, -1000L);
        credential.setRefreshToken(oldToken);

        when(credentialRepository.findByEmailOrUsername("fran@echoed.com")).thenReturn(Optional.of(credential));
        when(credentialRepository.save(any(CredentialEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.authenticateUser(new AuthRequest("Fran@Echoed.com", "password"));

        assertNotEquals(oldToken, response.refreshToken());
        assertTrue(jwtService.validateRefreshToken(response.refreshToken(), credential));

        ArgumentCaptor<CredentialEntity> saved = ArgumentCaptor.forClass(CredentialEntity.class);
        verify(credentialRepository).save(saved.capture());
        assertEquals(response.refreshToken(), saved.getValue().getRefreshToken());
    }
}
