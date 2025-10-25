package com.musicspring.app.music_app.security.oauth2.handlers;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.enums.Role;
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
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final String frontendRedirectUri;

    /**
     * @param jwtService           Service for JWT token operations
     * @param credentialRepository Repository for managing user credentials
     * @param userRepository       Repository for managing user entities
     * @param roleRepository       Repository for managing user roles
     */
    @Autowired
    public OAuth2AuthenticationSuccessHandler(JwtService jwtService,
                                              CredentialRepository credentialRepository,
                                              UserRepository userRepository,
                                              RoleRepository roleRepository, @Value("${app.oauth2.redirect-uri}") String frontendRedirectUri) {
        this.jwtService = jwtService;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
                                        Authentication authentication) throws IOException{

        // Extract OAuth2 user principal from authentication object
        Object principal = authentication.getPrincipal();
        String email = null;
        String name = null;

        // Handle different OAuth2 provider response formats
        // DefaultOidcUser is used for OpenID Connect providers (like Google)
        if (principal instanceof DefaultOidcUser oidcUser) {
            email = oidcUser.getEmail();           // Extract email from OIDC claims
            name = oidcUser.getFullName();         // Extract full name from OIDC claims
        }
        // OAuth2User is used for standard OAuth2 providers
        else if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");  // Extract email from OAuth2 attributes
            name = oauth2User.getAttribute("name");    // Extract name from OAuth2 attributes
        }

        // Email is required for user identification and account creation
        if (email == null) {
            throw new RuntimeException("Email not found in OAuth2 response");
        }

        // Find existing user or create new account with OAuth2 data
        CredentialEntity credential = findOrCreateOAuth2User(email, name);

        // Generate JWT token for frontend authentication
        String token = jwtService.generateToken(credential);

        // Redirect to frontend OAuth2 handler with authentication token
        // Frontend will extract token from URL and store for API authentication
        String targetUrl = UriComponentsBuilder.fromUriString(this.frontendRedirectUri)
                .queryParam("token", token)
                .queryParam("refresh_token", credential.getRefreshToken())
                .build().toUriString();

        // Perform the redirect to frontend application
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Finds an existing OAuth2 user or creates a new one.
     * This method implements the "find or create" pattern for OAuth2 users.
     * It first attempts to locate an existing credential by email address (case-insensitive).
     * If no existing credential is found, it creates a new user account.
     *
     * @param email User's email address from OAuth2 provider (required for identification)
     * @param name  User's display name from OAuth2 provider (optional, used for username generation)
     * @return CredentialEntity representing the user's authentication credentials
     */
    private CredentialEntity findOrCreateOAuth2User(String email, String name) {
        // Normalize email to lowercase to handle case sensitivity issues
        String normalizedEmail = email.toLowerCase().trim();
        
        Optional<CredentialEntity> existingCredential = credentialRepository.findByEmail(normalizedEmail);

        if (existingCredential.isPresent()) {
            CredentialEntity credential = existingCredential.get();

            // Update provider if the user was created locally but now logging in via OAuth2
            if (credential.getProvider() == AuthProvider.LOCAL) {
                credential.setProvider(AuthProvider.GOOGLE);
            }

            if (credential.getRefreshToken() == null) {
                String refreshToken = jwtService.generateRefreshToken(credential);
                credential.setRefreshToken(refreshToken);
                credentialRepository.save(credential);
            }

            return credential;
        } else {
            return createOAuth2User(normalizedEmail, name);
        }
    }

    /**
     * Creates a new OAuth2 user account.
     * This method creates both a UserEntity (application user) and CredentialEntity (authentication data)
     * for a new OAuth2 user. The process involves:
     * 1. Creating a UserEntity with generated username and active status
     * 2. Creating a CredentialEntity with OAuth2 provider information
     * 3. Assigning default user roles for authorization
     *
     * @param email User's email address from OAuth2 provider
     * @param name  User's display name from OAuth2 provider (used for username generation)
     * @return CredentialEntity for the newly created user
     */
    private CredentialEntity createOAuth2User(String email, String name) {
        // Generate a unique username to avoid conflicts
        String baseUsername = name != null ? name : email.split("@")[0];
        String uniqueUsername = generateUniqueUsername(baseUsername);
        
        // Create the main user entity with basic information
        UserEntity user = UserEntity.builder()
                .username(uniqueUsername)        // Use unique username to avoid conflicts
                .active(true)                    // New OAuth2 users are active by default
                .build();

        // Save user entity to database to generate ID
        user = userRepository.save(user);

        // Create credential entity with OAuth2 provider information
        CredentialEntity credential = CredentialEntity.builder()
                .email(email)                    // Store email for identification
                .provider(AuthProvider.GOOGLE)   // Currently only supporting Google OAuth2
                .user(user)                      // Link to the user entity
                .roles(getDefaultRoles())        // Assign default user roles
                .build();

        String refreshToken = jwtService.generateRefreshToken(credential);
        credential.setRefreshToken(refreshToken);

        // Save and return the credential entity
        return credentialRepository.save(credential);
    }

    /**
     * Retrieves default roles for new OAuth2 users.
     * This method assigns the standard USER role to new OAuth2 accounts.
     *
     * @return Set of RoleEntity objects representing default user permissions
     */
    private Set<RoleEntity> getDefaultRoles() {
        Set<RoleEntity> roles = new HashSet<>();

        // Find and assign the standard USER role
        // This role provides basic application access permissions
        roleRepository.findByRole(Role.ROLE_USER)
                .ifPresent(roles::add);

        return roles;
    }

    /**
     * Generates a unique username by appending numbers to the base username if conflicts exist.
     * This method prevents constraint violations when creating new users with OAuth2.
     * 
     * @param baseUsername The desired username
     * @return A unique username that doesn't exist in the database
     */
    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        // Keep trying until we find a username that doesn't exist
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}