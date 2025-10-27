package com.musicspring.app.music_app.security.oauth2.services;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.oauth2.dto.CustomOAuth2User;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * CustomOAuth2UserService handles OIDC user information processing (specifically for Google).
 * This service extends Spring's DefaultOidcUserService to customize how OIDC user
 * information is processed and stored in the application database. It is responsible for:
 * 1. Loading user information from OIDC providers (Google)
 * 2. Mapping OIDC user attributes to application user entities
 * 3. Creating or updating user records based on OIDC data
 * 4. Assigning appropriate roles to OIDC users
 * 5. Maintaining provider-specific information (provider ID, profile pictures, etc.)
 * The service ensures that OIDC users are properly integrated into the application's
 * user management and security system.
 */
@Service
// Extend DefaultOidcUserService instead of DefaultOAuth2UserService
public class CustomOAuth2UserService extends OidcUserService {

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    /**
     * Constructor for dependency injection.
     * @param credentialRepository Repository for managing user credentials
     * @param userRepository       Repository for managing user entities
     * @param roleRepository       Repository for managing user roles
     * @param jwtService           Service for managing JWT tokens
     */
    @Autowired
    public CustomOAuth2UserService(CredentialRepository credentialRepository,
                                   UserRepository userRepository,
                                   RoleRepository roleRepository, JwtService jwtService) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
    }

    /**
     * Loads the OIDC user and processes them.
     * This method is called by Spring Security during the OIDC authentication flow.
     * It delegates to the parent class to load basic OIDC user information, then
     * processes that information using custom logic to create or update application
     * user records and wrap the result in a CustomOAuth2User.
     * @param userRequest OidcUserRequest containing access token, ID token, and client registration
     * @return CustomOAuth2User containing OIDC attributes and application CredentialEntity data
     * @throws OAuth2AuthenticationException if user processing fails
     */
    @Override
    @Transactional // Ensures database operations are atomic
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Load the standard OIDC user information using the parent class method
        OidcUser oidcUser = super.loadUser(userRequest);

        // 2. Process the OIDC user data using custom logic to find/create CredentialEntity
        CredentialEntity credential = processOidcUser(oidcUser);

        // 3. Return your custom wrapper which should implement OidcUser
        // Ensure CustomOAuth2User's constructor accepts OidcIdToken and OidcUserInfo
        // and that CustomOAuth2User implements the OidcUser interface.
        return new CustomOAuth2User(oidcUser.getAttributes(), credential, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    /**
     * Processes OIDC user data and manages application user records.
     * Extracts user information from OIDC attributes (email, sub, name, picture),
     * then finds an existing CredentialEntity or creates a new one.
     * Handles both new user registration and existing user updates via OIDC.
     * @param oidcUser OidcUser containing standard OIDC claims and attributes
     * @return CredentialEntity representing the persisted application user credentials
     */
    private CredentialEntity processOidcUser(OidcUser oidcUser) {
        // Extract standard OIDC user information
        String email = oidcUser.getEmail();               // Email address (required)
        String googleId = oidcUser.getSubject();          // Google's unique user identifier ('sub' claim)
        String name = oidcUser.getFullName();             // Display name ('name' claim)
        String pictureUrl = oidcUser.getPicture();        // Profile picture URL ('picture' claim)

        // Email is essential for linking accounts
        if (email == null) {
            // Handle cases where email might be missing (though unlikely for Google)
            throw new OAuth2AuthenticationException("Email not found in OIDC response");
        }

        // Find existing user credential by email (case-insensitive recommended)
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByEmailOrUsername(email);
        CredentialEntity credential;

        if (credentialOptional.isPresent()) {
            // User exists - update their info
            credential = credentialOptional.get();

            // If user previously logged in locally, update provider
            if (credential.getProvider() == AuthProvider.LOCAL) {
                credential.setProvider(AuthProvider.GOOGLE);
            }
            // Always update providerId and pictureUrl with latest info from Google
            credential.setProviderId(googleId);
            credential.setProfilePictureUrl(pictureUrl);

            // Generate refresh token if it's missing (e.g., first OAuth login for existing user)
            if (credential.getRefreshToken() == null) {
                String refreshToken = jwtService.generateRefreshToken(credential);
                credential.setRefreshToken(refreshToken);
            }
            // Save potential updates
            credential = credentialRepository.save(credential);
        } else {
            // User does not exist - create a new account
            credential = createNewOidcUser(email, googleId, name, pictureUrl);
            // The createNewOidcUser method already saves the new credential
        }

        return credential; // Return the persisted CredentialEntity
    }

    /**
     * Creates a new user account based on OIDC information.
     * Creates both UserEntity and CredentialEntity records, assigns default roles,
     * and generates a refresh token.
     * @param email      User's email address from OIDC provider
     * @param googleId   Google's unique identifier ('sub') for the user
     * @param name       User's display name from OIDC provider
     * @param pictureUrl URL to user's profile picture from OIDC provider
     * @return CredentialEntity for the newly created and persisted user
     */
    private CredentialEntity createNewOidcUser(String email, String googleId, String name, String pictureUrl) {
        // Generate a unique username if the provided name is taken or null
        String baseUsername = name != null && !name.trim().isEmpty() ? name : email.split("@")[0];
        String uniqueUsername = generateUniqueUsername(baseUsername);

        // Create the main user entity
        UserEntity user = UserEntity.builder()
                .username(uniqueUsername) // Use the generated unique username
                .active(true)             // New OIDC users are active by default
                .build();
        // Save user first to get the ID
        user = userRepository.save(user);

        // Create the credential entity linked to the user
        CredentialEntity credential = CredentialEntity.builder()
                .email(email)                        // Store email
                .provider(AuthProvider.GOOGLE)       // Set provider to GOOGLE
                .providerId(googleId)                // Store Google's unique ID
                .profilePictureUrl(pictureUrl)       // Store profile picture URL
                .user(user)                          // Link to the UserEntity
                .roles(getDefaultRoles())            // Assign default roles (e.g., ROLE_USER)
                .build();

        // Generate and set the refresh token
        String refreshToken = jwtService.generateRefreshToken(credential);
        credential.setRefreshToken(refreshToken);

        // Save and return the newly created credential
        return credentialRepository.save(credential);
    }

    /**
     * Retrieves default roles (ROLE_USER) for new users.
     * @return A Set containing the default RoleEntity.
     */
    private Set<RoleEntity> getDefaultRoles() {
        Set<RoleEntity> roles = new HashSet<>();
        roleRepository.findByRole(Role.ROLE_USER)
                // Add the role to the set if found, otherwise throw an exception
                .ifPresentOrElse(roles::add,
                        () -> { throw new RuntimeException("Default role ROLE_USER not found in database during OAuth2 user creation."); }
                );
        return roles;
    }

    /**
     * Generates a unique username by appending numbers if the base username already exists.
     * @param baseUsername The desired base username (e.g., from user's name or email prefix)
     * @return A unique username guaranteed not to exist in the database.
     */
    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername.replaceAll("[^a-zA-Z0-9.-]", "_"); // Sanitize username slightly
        int counter = 1;
        // Keep appending numbers until a unique username is found
        while (userRepository.findByUsername(username).isPresent()) { // Assumes findByUsername exists
            username = baseUsername + counter;
            counter++;
            // Optional: Add a limit to prevent infinite loops in extreme edge cases
            if (counter > 100) {
                throw new RuntimeException("Could not generate a unique username for: " + baseUsername);
            }
        }
        return username;
    }
}