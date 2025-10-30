package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.request.UserUpdateRequest;
import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.security.dto.AuthResponse;
import com.musicspring.app.music_app.security.dto.CompleteProfileRequest;
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
        // Llama al método del servicio que devuelve el perfil completo con stats
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
            summary = "Reactivate a user by ID",
            description = "Logically reactivates a user by setting their active status to true, including their reviews and comments."
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivateUser(
            @Parameter(description = "Internal user ID", example = "1")
            @PathVariable Long id) {
        userService.reactivateUser(id);
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