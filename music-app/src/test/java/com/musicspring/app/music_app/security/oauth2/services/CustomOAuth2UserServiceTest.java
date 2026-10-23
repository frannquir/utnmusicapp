package com.musicspring.app.music_app.security.oauth2.services;

import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.TestTokens;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.oauth2.dto.CustomOAuth2User;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.security.service.JwtService;
import com.musicspring.app.music_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock private CredentialRepository credentialRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserService userService;

    @Test
    void googleSignInReplacesOldStoredRefreshToken() {
        JwtService jwtService = TestTokens.jwtService();
        CustomOAuth2UserService service = new CustomOAuth2UserService(
                credentialRepository, userRepository, roleRepository, jwtService, userService);

        CredentialEntity credential = TestTokens.credential(AuthProvider.GOOGLE);
        String oldToken = TestTokens.refreshToken(jwtService, credential, -1000L);
        credential.setRefreshToken(oldToken);

        when(credentialRepository.findByEmailIgnoreCase("fran@echoed.com")).thenReturn(Optional.of(credential));
        when(credentialRepository.save(any(CredentialEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomOAuth2User user = (CustomOAuth2User) service.loadUser(googleRequest());

        String issued = user.getCredential().getRefreshToken();
        assertNotEquals(oldToken, issued);
        assertTrue(jwtService.validateRefreshToken(issued, credential));

        ArgumentCaptor<CredentialEntity> saved = ArgumentCaptor.forClass(CredentialEntity.class);
        verify(credentialRepository).save(saved.capture());
        assertEquals(issued, saved.getValue().getRefreshToken());
    }

    // no user-info URI, so OidcUserService builds the user from the ID token without calling Google
    private OidcUserRequest googleRequest() {
        Instant now = Instant.now();
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userNameAttributeName("email")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "access-token", now, now.plusSeconds(60), Set.of("openid", "email"));
        OidcIdToken idToken = OidcIdToken.withTokenValue("id-token")
                .subject("google-123")
                .claim("email", "fran@echoed.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
        return new OidcUserRequest(registration, accessToken, idToken);
    }
}
