 package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.request.AlbumReviewRequest;
import com.musicspring.app.music_app.model.dto.request.ReviewPatchRequest;
import com.musicspring.app.music_app.model.dto.response.AlbumReviewResponse;
import com.musicspring.app.music_app.service.AlbumReviewService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/albumreviews")
@Tag(name = "Album Reviews", description = "Operations related to album reviews management")
public class AlbumReviewController {
    private final AlbumReviewService albumReviewService;

    @Autowired
    public AlbumReviewController(AlbumReviewService albumReviewService) {
        this.albumReviewService = albumReviewService;
    }

    @Operation(
            summary = "Retrieve all album reviews",
            description = "Fetches a paginated list of all album reviews, sorted by the specified parameter."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Album reviews retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, invalid parameters.",
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
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Page<AlbumReviewResponse>> getAllAlbumReviews(
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "date")
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        return ResponseEntity.ok(albumReviewService.findAll(pageable));
    }

    @Operation(
            summary = "Retrieve an album review by ID",
            description = "Fetches the details of a specific album review based on its unique ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Album review retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AlbumReviewResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Album review not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AlbumReviewResponse> getAlbumReviewById(
            @Parameter(description = "ID of the album review to retrieve", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(albumReviewService.findById(id));
    }

    @Operation(
            summary = "Create a new album review",
            description = "Creates a new album review with the provided data. Requires either albumId (for existing albums) or spotifyId (for Spotify albums). The userId in the request must match the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Album review created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AlbumReviewResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or missing required identifiers",
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
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - you can only create reviews for yourself",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Album or user not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @PostMapping()
    public ResponseEntity<AlbumReviewResponse> createAlbumReview(
            @Parameter(description = "ID of the album to review (for existing albums in database)", example = "1")
            @RequestParam(required = false) Long albumId,
            @Parameter(description = "Spotify ID of the album to review (for albums from Spotify)", example = "4eLPsYPBmXABThSJ821sqY")
            @RequestParam(required = false) String spotifyId,
            @Parameter(description = "Data for the new album review", required = true)
            @Valid @RequestBody AlbumReviewRequest albumReviewRequest) {

        AlbumReviewResponse savedReview = albumReviewService.createAlbumReview(
                albumId, spotifyId, albumReviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    @Operation(
            summary = "Retrieve album reviews by user ID",
            description = "Fetches a paginated list of album reviews submitted by a specific user, sorted by the specified parameter."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Album reviews retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, invalid parameters",
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
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AlbumReviewResponse>> getAlbumReviewsByUserId(
            @Parameter(description = "ID of the user whose album reviews are to be retrieved", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Number of items per page", example = "10", required = true)
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0", required = true)
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "reviewId", required = true)
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<AlbumReviewResponse> albumReviewResponsePage = albumReviewService.findByUserId(userId, pageable);
        return ResponseEntity.ok(albumReviewResponsePage);
    }

    @Operation(
            summary = "Get all reviews for a specific album",
            description = "Retrieves a paginated list of reviews for a given album. You can provide either an albumId (for existing albums in the database) or a spotifyId (for albums from Spotify). Pagination and sorting are supported."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of album reviews",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid album identifiers, or invalid pagination/sorting parameters",
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
            @ApiResponse(
                    responseCode = "404",
                    description = "Album not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping("/albums")
    public ResponseEntity<Page<AlbumReviewResponse>> getAlbumReviewsByAlbum(
            @Parameter(description = "ID of the album whose reviews are requested (for existing albums)", example = "123")
            @RequestParam(required = false) Long albumId,
            @Parameter(description = "Spotify ID of the album whose reviews are requested (for Spotify albums)", example = "4eLPsYPBmXABThSJ821sqY")
            @RequestParam(required = false) String spotifyId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "date")
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<AlbumReviewResponse> albumReviewResponsePage = albumReviewService.findByAlbum(albumId, spotifyId, pageable);
        return ResponseEntity.ok(albumReviewResponsePage);
    }

    @Operation(
            summary = "Delete an album review",
            description = "Performs a logical delete by setting the review's 'active' field to false. Only the owner of the review can perform this action."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Review deleted successfully (logically). No content is returned."
            ),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized: Authentication is required.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden: You don't have permission to delete this review.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Review not found",
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
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Void> deleteAlbumReview(
            @Parameter(description = "Review ID", example = "10", required = true)
            @PathVariable Long reviewId) {
        albumReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
    @Operation(
            summary = "Update the content of an existing album review",
            description = "Modifies the content of a album review identified by its ID. Only the owner of the review can perform this action."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AlbumReviewResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - user does not own the review",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @PatchMapping("/{albumReviewId}")
    public ResponseEntity<AlbumReviewResponse> updateAlbumReviewContent(
            @PathVariable Long albumReviewId,
            @RequestBody ReviewPatchRequest patchRequest) {
        AlbumReviewResponse updated = albumReviewService.updateAlbumReviewContent(albumReviewId, patchRequest);
        return ResponseEntity.ok(updated);
    }
}