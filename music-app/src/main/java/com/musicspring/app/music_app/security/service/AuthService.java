package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.exception.AccountDeactivatedException;
import com.musicspring.app.music_app.model.dto.request.SignupRequest;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.model.mapper.CredentialMapper;
import com.musicspring.app.music_app.model.mapper.UserMapper;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.dto.AuthRequest;
import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.mapper.AuthMapper;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {
    private final CredentialRepository credentialsRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CredentialMapper credentialMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthMapper authMapper;


    @Autowired
    public AuthService(CredentialRepository credentialsRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository,
                       UserMapper userMapper,
                       CredentialMapper credentialMapper,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, AuthMapper authMapper) {
        this.credentialsRepository = credentialsRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.credentialMapper = credentialMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthResponse authenticateUser(AuthRequest authRequest) {
        AuthRequest normalizedRequest = new AuthRequest(
                authRequest.emailOrUsername().toLowerCase(),
                authRequest.password()
        );

        CredentialEntity user = authenticate(normalizedRequest);
        String token = jwtService.generateToken(user);

        return authMapper.toAuthResponse(user, token);
    }

    public CredentialEntity authenticate(AuthRequest input) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.emailOrUsername(),
                            input.password()
                    )
            );

        } catch (BadCredentialsException e) {
            throw e;

        } catch (LockedException e) {
            CredentialEntity credential = credentialsRepository
                    .findByEmailOrUsername(input.emailOrUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            if (credential.getUser() != null && !credential.getUser().getActive()) {
                throw new AccountDeactivatedException(
                        "Account is deactivated",
                        credential.getUser().getUserId()
                );
            } else {
                throw e;
            }
        }

        CredentialEntity credential = credentialsRepository.findByEmailOrUsername(input.emailOrUsername())
                .orElseThrow(()->new UsernameNotFoundException("User not found after successful authentication"));

        if (credential.getUser() != null && !credential.getUser().getActive()) {
            throw new AccountDeactivatedException(
                    "Account is deactivated",
                    credential.getUser().getUserId()
            );
        }
        return credential;
    }
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);

        CredentialEntity credentialEntity = credentialsRepository.findByEmail(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        if(!credentialEntity.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh token does not match");
        }

        if (!jwtService.validateRefreshToken(refreshToken,credentialEntity)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateToken(credentialEntity);
        String newRefreshToken = jwtService.generateRefreshToken(credentialEntity);
        credentialEntity.setRefreshToken(newRefreshToken);
        credentialsRepository.save(credentialEntity);
      
        return authMapper.toAuthResponse(credentialEntity, newAccessToken);
    }

    @Transactional
    public AuthResponse registerUserWithEmail(SignupRequest signupRequest) {

        String normalizedEmail = signupRequest.getEmail().toLowerCase();

        if (credentialsRepository.findByEmailIgnoreCase(normalizedEmail).isPresent())
            throw new IllegalArgumentException("User already exists with email: " + signupRequest.getEmail());

        if (userRepository.existsByUsernameIgnoreCase(signupRequest.getUsername()))
            throw new IllegalArgumentException("Username already taken: " + signupRequest.getUsername());

        UserEntity user = userMapper.toUserEntity(signupRequest);

        user = userRepository.save(user);

        CredentialEntity credential = credentialMapper.toCredentialEntity(signupRequest, user);

        credential.setEmail(normalizedEmail);
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        credential.setRoles(Set.of(roleRepository
                .findByRole(Role.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("Default role ROLE_USER not found."))));

        credential.setRefreshToken(jwtService.generateRefreshToken(credential));

        credential = credentialsRepository.save(credential);

        String token = jwtService.generateToken(credential);

        return authMapper.toAuthResponse(credential, token);
    }

    public static Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof CredentialEntity credential) {
            if (credential.getUser() != null) {
                return credential.getUser().getUserId();
            }
        }

        throw new AccessDeniedException("Unable to extract user ID from authentication");
    }

    public static void validateUserOwnership(Long authenticatedUserId, Long requestedUserId) {
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AccessDeniedException("You cannot perform this action for another user.");
        }
    }

    public static void validateRequestUserOwnership(Long requestedUserId) {
        Long authenticatedUserId = extractUserId();
        validateUserOwnership(authenticatedUserId, requestedUserId);
    }
}