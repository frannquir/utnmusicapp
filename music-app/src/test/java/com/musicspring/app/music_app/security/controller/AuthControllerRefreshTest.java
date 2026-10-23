package com.musicspring.app.music_app.security.controller;

import com.musicspring.app.music_app.exception.GlobalExceptionHandler;
import com.musicspring.app.music_app.model.mapper.CredentialMapper;
import com.musicspring.app.music_app.model.mapper.SecurityMapper;
import com.musicspring.app.music_app.model.mapper.UserMapper;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.TestTokens;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.mapper.AuthMapper;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.EmailVerificatorTokenRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.security.service.AuthService;
import com.musicspring.app.music_app.security.service.EmailVerificatorService;
import com.musicspring.app.music_app.security.service.JwtService;
import com.musicspring.app.music_app.security.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Real AuthService + JwtService behind the controller, only the repositories are mocked.
@ExtendWith(MockitoExtension.class)
class AuthControllerRefreshTest {

    private static final String INVALID_MESSAGE = "Refresh token is invalid or expired. Please sign in again.";

    @Mock private CredentialRepository credentialRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private CredentialMapper credentialMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleRepository roleRepository;
    @Mock private EmailVerificatorService emailVerificatorService;
    @Mock private EmailVerificatorTokenRepository emailVerificatorTokenRepository;
    @Mock private PasswordResetService passwordResetService;

    private JwtService jwtService;
    private MockMvc mockMvc;
    private CredentialEntity credential;

    @BeforeEach
    void setUp() {
        jwtService = TestTokens.jwtService();
        AuthService authService = new AuthService(credentialRepository, authenticationManager, jwtService,
                userRepository, userMapper, credentialMapper, passwordEncoder, roleRepository,
                new AuthMapper(new SecurityMapper()), emailVerificatorService, emailVerificatorTokenRepository);

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, passwordResetService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        credential = TestTokens.credential(AuthProvider.LOCAL);
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"));
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        String expired = TestTokens.refreshToken(jwtService, credential, -1000L);
        credential.setRefreshToken(expired);

        refresh(expired)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_MESSAGE));

        verify(credentialRepository, never()).save(any());
    }

    @Test
    void malformedTokenReturns401() throws Exception {
        refresh("not-a-jwt")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_MESSAGE));
    }

    @Test
    void tokenNotMatchingStoredOneReturns401() throws Exception {
        // different lifetimes so the two tokens can't be identical
        credential.setRefreshToken(TestTokens.refreshToken(jwtService, credential, 60_000L));
        String other = TestTokens.refreshToken(jwtService, credential, 120_000L);
        when(credentialRepository.findByEmail("fran@echoed.com")).thenReturn(Optional.of(credential));

        refresh(other)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_MESSAGE));

        verify(credentialRepository, never()).save(any());
    }

    @Test
    void validTokenReturns200AndRotatesStoredToken() throws Exception {
        String current = TestTokens.refreshToken(jwtService, credential, 60_000L);
        credential.setRefreshToken(current);
        when(credentialRepository.findByEmail("fran@echoed.com")).thenReturn(Optional.of(credential));
        when(credentialRepository.save(any(CredentialEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        refresh(current)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(not(current)));

        ArgumentCaptor<CredentialEntity> saved = ArgumentCaptor.forClass(CredentialEntity.class);
        verify(credentialRepository).save(saved.capture());
        assertNotEquals(current, saved.getValue().getRefreshToken());
    }
}
