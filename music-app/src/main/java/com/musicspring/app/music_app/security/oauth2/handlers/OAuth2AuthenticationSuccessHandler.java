package com.musicspring.app.music_app.security.oauth2.handlers;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.oauth2.dto.CustomOAuth2User;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.security.service.JwtService;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * OAuth2AuthenticationSuccessHandler handles successful OAuth2 authentication flows.
 * <p>
 * This handler is responsible for:
 * 1. Processing successful OAuth2 authentication from providers, Google in this case
 * 2. Extracting user information from the OAuth2 response
 * 3. Creating or updating user records in the database
 * 4. Generating JWT tokens for session management
 * 5. Redirecting users to the frontend application with authentication tokens
 * <p>
 * The flow works as follows:
 * - User initiates OAuth2 login from frontend
 * - Provider redirects to backend with authorization code
 * - Spring Security handles the OAuth2 flow and calls this handler on success
 * - Handler extracts user data, manages database records, generates JWT
 * - User is redirected to frontend with JWT token for subsequent API calls
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final String frontendRedirectUri;

    /**
     * @param jwtService           Service for JWT token operations
     */
    @Autowired
    public OAuth2AuthenticationSuccessHandler(JwtService jwtService,
                                              @Value("${app.oauth2.redirect-uri}") String frontendRedirectUri) {
        this.jwtService = jwtService;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    /**
     * Handles successful OAuth2 authentication.
     * This method is called by Spring Security when OAuth2 authentication succeeds.
     * It processes the authentication result, manages user data, and redirects to frontend.
     * Process flow:
     * 1. Extract user information from OAuth2 provider response
     * 2. Find existing user or create new user account
     * 3. Generate JWT token for session management
     * 4. Redirect to frontend with token for subsequent API authentication
     *
     * @param request        HttpServletRequest containing the authentication request
     * @param response       HttpServletResponse for sending the redirect response
     * @param authentication Authentication object containing OAuth2 user data
     * @throws IOException      if redirect operation fails
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomOAuth2User customOAuth2User)) {
            logger.error("Error: Principal is not an instance of CustomOAuth2User");
            getRedirectStrategy().sendRedirect(request, response, "/error?message=AuthenticationPrincipalError");
            return;
        }

        CredentialEntity credential = customOAuth2User.getCredential();
        if (credential == null) {
            logger.error("Error: CredentialEntity is null inside CustomOAuth2User");
            getRedirectStrategy().sendRedirect(request, response, "/error?message=CredentialEntityNullError");
            return;
        }

        String token = jwtService.generateToken(credential);
        String refreshToken = credential.getRefreshToken();

        String targetUrl = UriComponentsBuilder.fromUriString(this.frontendRedirectUri)
                .queryParam("token", token)
                .queryParam("refresh_token", refreshToken != null ? refreshToken : "")
                .build().toUriString();

        clearAuthenticationAttributes(request);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}