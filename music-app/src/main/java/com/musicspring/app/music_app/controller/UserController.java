package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.request.PasswordUpdateRequest;
import com.musicspring.app.music_app.model.dto.request.UserUpdateRequest;
import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.dto.CompleteProfileRequest;
import com.musicspring.app.music_app.security.dto.DeactivateAccountRequest;
import com.musicspring.app.music_app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints related to user management")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users in the system, returning their full profiles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
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
    @GetMapping("")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
            summary = "Get all active users",
            description = "Retrieves a list of all active registered users in the system, returning their full profiles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Active users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
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
    @GetMapping("/active")
    public ResponseEntity<List<UserProfileResponse>> getAllActiveUsers() {
        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    @Operation(
            summary = "Get a user by ID",
            description = "Retrieves the full user profile (including stats) using their internal unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
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
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(
            @Parameter(description = "Internal user ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(
            summary = "Get a user by username",
            description = "Retrieves the full user profile (including stats) using their unique username."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
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
    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileResponse> getUserByUsername(
            @Parameter(description = "Username to search for", example = "johndoe123")
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @Operation(
            summary = "Delete a user by ID",
            description = "Logically deactivates a user account (sets active to false)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "User deactivated successfully",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "403",
                    description = "You can only deactivate your own account",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
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
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
                            @Parameter(description = "Internal user ID", example = "1")
                            @PathVariable Long id) {
        userService.deleteUser(id);
    }


    @Operation(
            summary = "Search users by username",
            description = "Performs a case-insensitive search for users whose usernames contain the specified query string, returning paginated full profiles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
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
    @GetMapping("/search")
    public ResponseEntity<Page<UserProfileResponse>> searchUsers(
            @Parameter(description = "Search query for username", example = "john")
            @RequestParam String query,
            @Parameter(hidden = true)
            Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(query, pageable));
    }

    @Operation(
            summary = "Change the current user's password",
            description = "Updates the authenticated user's password. Requires current and new password. This endpoint only works for users with local authentication (not Google)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Password changed successfully"
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request (e.g., new passwords don't match, or password blank)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed (e.g., incorrect current password)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "403",
                    description = "User is not authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "422",
                    description = "Cannot change password for a non-local account (e.g., Google)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PutMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Request containing current and new password", required = true)
            @Valid @RequestBody PasswordUpdateRequest request,
            Authentication authentication) {

        userService.updatePassword(request, authentication);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deactivate the current user's account",
            description = "Logically deactivates the authenticated user's account. If the user is LOCAL, the password is required. If GOOGLE, it is not."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Account deactivated successfully"
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request (e.g., password blank)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed (e.g., incorrect password)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "403",
                    description = "User is not authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivateAccount(
            @Parameter(description = "Request containing the user's current password (if local)", required = false)
            @RequestBody DeactivateAccountRequest request,
            Authentication authentication) {

        String authenticatedUserEmail = authentication.getName();

        userService.deactivateAccount(authenticatedUserEmail, request);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Request account reactivation",
            description = "Sends a verification email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "User reactivated successfully",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "User not found or already active",
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
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable Long id) {
        userService.requestReactivation(id);
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Verification code sent to your email."
        ));
    }

    @Operation(
            summary = "[ADMIN] Ban a user by ID",
            description = "Logically deactivates and bans a user account by setting active=false and isBanned=true. This action can only be performed by an ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User banned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Not an ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "User is already banned")
    })
    @PostMapping("/{id}/ban")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void banUser(
            @Parameter(description = "Internal user ID to ban", example = "1")
            @PathVariable Long id) {
        userService.banUser(id);
    }

    @Operation(
            summary = "[ADMIN] Unban a user by ID",
            description = "Logically reactivates and unbans a user account by setting active=true and isBanned=false. This action can only be performed by an ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User unbanned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Not an ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found or is not inactive"),
            @ApiResponse(responseCode = "400", description = "User is not currently banned")
    })
    @PostMapping("/{id}/unban")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbanUser(
            @Parameter(description = "Internal user ID to unban", example = "1")
            @PathVariable Long id) {
        userService.unbanUser(id);
    }

    @Operation(
            summary = "Get current logged-in user's profile",
            description = "Retrieves the full profile, including stats, roles, and permissions, for the user making the request based on their authentication token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication required to access this resource",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "User associated with the token not found",
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
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getCurrentUser(authentication));
    }

    @Operation(
            summary = "Update user profile",
            description = "Updates the profile information (username, picture, bio) for a specific user ID. Requires user to be authenticated and updating their own profile."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User profile updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input data (e.g., username already taken)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden: You can only update your own profile",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
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
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Parameter(description = "ID of the user to update", example = "1")
            @PathVariable Long id,
            @Parameter(description = "User profile update data")
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(id, request));
    }

    @Operation(
            summary = "Complete a new user's profile",
            description = "Sets the username for a newly registered user (e.g., via OAuth) who has an incomplete profile. Requires ROLE_INCOMPLETE_PROFILE."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Profile completed successfully. Returns a new AuthResponse with updated roles.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid username (e.g., already taken, invalid format)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "User is not authenticated.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "403",
                    description = "User does not have permission (e.g., profile already complete)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PostMapping("/complete-profile")
    public ResponseEntity<AuthResponse> completeProfile(
            @Parameter(description = "Request containing the new username", required = true)
            @Valid @RequestBody CompleteProfileRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String authenticatedUserEmail = authentication.getName();
        AuthResponse authResponse = userService.completeOAuthProfile(request, authenticatedUserEmail);

        return ResponseEntity.ok(authResponse);
    }
}