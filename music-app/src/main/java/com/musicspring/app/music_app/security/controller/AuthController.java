package com.musicspring.app.music_app.security.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.request.SignupRequest;
import com.musicspring.app.music_app.security.dto.AuthRequest;
import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.dto.RefreshTokenRequest;
import com.musicspring.app.music_app.security.dto.VerifyTokenRequest;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.service.AuthService;
import com.musicspring.app.music_app.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Operations related to user authentication, registration, and token management")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Authenticate user and return access token",
            description = "Authenticates a user using username/email and password, and returns a JWT access token along with user info."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PostMapping()
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.authenticateUser(authRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "403",
                    description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken (@RequestBody RefreshTokenRequest refreshTokenRequest) {
        AuthResponse response = authService.refreshAccessToken(refreshTokenRequest.refreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Register a new user with email",
            description = "Creates a new user account with username, email, and password. Returns authentication token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User registered successfully with authentication token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid registration data, username or email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUserWithEmail(
            @Valid @RequestBody SignupRequest signupRequest) {

        return authService.registerUserWithEmail(signupRequest);
    }

    @Operation(
            summary = "Verify email account",
            description = "Activates the user account using the 6-character code sent via email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(@Valid @RequestBody VerifyTokenRequest request) {
        authService.verifyAccount(request.token());

        return ResponseEntity.ok(Map.of(
                "message", "¡Account successfully verified! Please login to continue."
        ));
    }
}
