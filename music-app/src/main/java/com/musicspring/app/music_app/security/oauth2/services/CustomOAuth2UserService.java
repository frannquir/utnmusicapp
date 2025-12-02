package com.musicspring.app.music_app.security.oauth2.services;

import com.musicspring.app.music_app.exception.AccountBannedException;
import com.musicspring.app.music_app.exception.AccountDeactivatedException;
import com.musicspring.app.music_app.model.enums.DefaultAvatar;
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
import com.musicspring.app.music_app.service.UserService;
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
import org.springframework.security.oauth2.core.OAuth2Error;
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
    private final UserService userService;

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
                                   RoleRepository roleRepository,
                                   JwtService jwtService,
                                   UserService userService) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.userService = userService;
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
        // 1. Load the standard OIDC user information
        OidcUser oidcUser = super.loadUser(userRequest);
        CredentialEntity credential;

        try {
            // 2. Process the OIDC user
            credential = processOidcUser(oidcUser);

        } catch (AccountBannedException ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error("account_banned"), ex);

        } catch (AccountDeactivatedException ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error("account_deactivated"), ex);

        } catch (Exception ex) {
            System.err.println("Unexpected error during OIDC user processing: " + ex.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error("internal_error"), ex);
        }
        return new CustomOAuth2User(oidcUser.getAttributes(), credential, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    /**
     * Processes OIDC user data (email, googleId) and manages application user records.
     * Finds an existing CredentialEntity or creates a new one with an INCOMPLETE profile status.
     *
     * @param oidcUser OidcUser containing standard OIDC claims and attributes (email, sub).
     * @return CredentialEntity representing the persisted application user (either complete or incomplete).
     */
    private CredentialEntity processOidcUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String googleId = oidcUser.getSubject();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not found in OIDC response");
        }

        String normalizedEmail = email.toLowerCase();

        Optional<CredentialEntity> credentialOptional = credentialRepository.findByEmailIgnoreCase(normalizedEmail);
        CredentialEntity credential;

        if (credentialOptional.isPresent()) {
            credential = credentialOptional.get();

            UserEntity user = credential.getUser();
            if (user != null) {
                if (user.getIsBanned()) {

                    throw new AccountBannedException("This account has been banned by an administrator.");
                }
                if (!user.getActive()) {
                    throw new AccountDeactivatedException("Account is deactivated", user.getUserId());
                }
            }

            if (credential.getProvider() == AuthProvider.LOCAL) {
                credential.setProvider(AuthProvider.GOOGLE);
            }

            credential.setProviderId(googleId);

            if(credential.getUser() != null && !credential.getUser().getActive()){
                userService.performReactivation(credential.getUser());

                credential = credentialRepository.findByEmailIgnoreCase(normalizedEmail)
                        .orElseThrow(() -> new OAuth2AuthenticationException("Failed to refetch reactivated user."));
            }

            if (credential.getRefreshToken() == null) {
                String refreshToken = jwtService.generateRefreshToken(credential);
                credential.setRefreshToken(refreshToken);
            }

            credential = credentialRepository.save(credential);

        } else {
            credential = createNewOidcUser(normalizedEmail, googleId);
        }

        return credential;
    }

    /**
     * Creates a new, incomplete user account for an OIDC login.
     * Sets a temporary username and assigns ROLE_INCOMPLETE_PROFILE.
     *
     * @param email     User's normalized (lowercase) email address.
     * @param googleId   Google's unique identifier ('sub') for the user.
     * @return CredentialEntity for the newly created and persisted user.
     */
    private CredentialEntity createNewOidcUser(String email, String googleId) {

        String tempUsername = "_google_" + googleId;

        UserEntity user = UserEntity.builder()
                .username(tempUsername)
                .active(true)
                .isBanned(false)
                .build();
        user = userRepository.save(user);

        RoleEntity incompleteRole = roleRepository.findByRole(Role.ROLE_INCOMPLETE_PROFILE)
                .orElseThrow(() -> new RuntimeException("Default role ROLE_INCOMPLETE_PROFILE not found in database."));

        CredentialEntity credential = CredentialEntity.builder()
                .email(email)
                .provider(AuthProvider.GOOGLE)
                .providerId(googleId)
                .user(user)
                .roles(Set.of(incompleteRole))
                .profilePictureUrl(DefaultAvatar.getRandomAvatarFileName())
                .build();

        String refreshToken = jwtService.generateRefreshToken(credential);
        credential.setRefreshToken(refreshToken);

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