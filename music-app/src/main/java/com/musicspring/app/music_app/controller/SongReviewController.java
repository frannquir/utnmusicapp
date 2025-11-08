package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.request.ReviewUpdateRequest;
import com.musicspring.app.music_app.model.dto.request.SongReviewRequest;
import com.musicspring.app.music_app.model.dto.response.CommentResponse;
import com.musicspring.app.music_app.model.dto.response.SongReviewResponse;
import com.musicspring.app.music_app.service.SongReviewService;
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
@RequestMapping("api/v1/songreviews")
@Tag(name = "Song Reviews", description = "Operations related to song reviews")
public class SongReviewController {

    private final SongReviewService songReviewService;

    @Autowired
    public SongReviewController(SongReviewService songReviewService) {
        this.songReviewService = songReviewService;
    }


    @Operation(
            summary = "Get all song reviews",
            description = "Retrieves a paginated list of all song reviews. Allows specifying the page number, page size, and sorting field."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of song reviews",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SongReviewResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or sorting parameters",
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
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping()
    public ResponseEntity<Page<SongReviewResponse>> getAllSongReviews(
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "date")
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        return ResponseEntity.ok(songReviewService.findAll(pageable));
    }


    @Operation(
            summary = "Get a song review by ID",
            description = "Retrieves the details of a specific song review by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the song review",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SongReviewResponse.class)
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
                    description = "Song review not found with the provided ID",
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
    @GetMapping("/{id}")
    public ResponseEntity<SongReviewResponse> getSongReviewById(
            @Parameter(description = "ID of the song review to retrieve", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(songReviewService.findById(id));
    }

    @Operation(
            summary = "Create a new song review",
            description = "Creates a new song review with the provided data. Requires either songId (for existing songs) or spotifyId (for Spotify songs). The userId in the request must match the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Song review created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SongReviewResponse.class)
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
                    description = "Song or user not found",
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
    public ResponseEntity<SongReviewResponse> createSongReview(
            @Parameter(description = "ID of the song to review (for existing songs in database)", example = "123")
            @RequestParam(required = false) Long songId,
            @Parameter(description = "Spotify ID of the song to review (for songs from Spotify)", example = "4iV5W9uYEdYUVa79Axb7Rh")
            @RequestParam(required = false) String spotifyId,
            @Parameter(description = "Data for the new song review", required = true)
            @Valid @RequestBody SongReviewRequest songReviewRequest) {

        SongReviewResponse savedReview = songReviewService.createSongReview(
                songId, spotifyId, songReviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }


    @Operation(
            summary = "Get all song reviews by user ID",
            description = "Retrieves a paginated list of song reviews submitted by a specific user, identified by their user ID. Supports pagination and sorting."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of song reviews by user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SongReviewResponse.class)
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
                    responseCode = "400",
                    description = "Invalid pagination or sorting parameters",
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
    public ResponseEntity<Page<SongReviewResponse>> getSongReviewsByUserId(
            @Parameter(description = "ID of the user whose song reviews are requested", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "date")
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<SongReviewResponse> songReviewResponsePage = songReviewService.findByUserId(userId, pageable);
        return ResponseEntity.ok(songReviewResponsePage);
    }


    @Operation(
            summary = "Get all reviews for a specific song",
            description = "Retrieves a paginated list of reviews for a given song. You can provide either a songId (for existing songs in the database) or a spotifyId (for songs from Spotify). Pagination and sorting are supported."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of song reviews",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SongReviewResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid song identifiers, or invalid pagination/sorting parameters",
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
                    description = "Song not found",
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
    @GetMapping("/songs")
    public ResponseEntity<Page<SongReviewResponse>> getSongReviewsBySong(
            @Parameter(description = "ID of the song whose reviews are requested (for existing songs)", example = "123")
            @RequestParam(required = false) Long songId,
            @Parameter(description = "Spotify ID of the song whose reviews are requested (for Spotify songs)", example = "4iV5W9uYEdYUVa79Axb7Rh")
            @RequestParam(required = false) String spotifyId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "date")
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<SongReviewResponse> songReviewResponsePage = songReviewService.findBySong(songId, spotifyId, pageable);
        return ResponseEntity.ok(songReviewResponsePage);
    }

    @Operation(
            summary = "Delete a song review",
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
    public ResponseEntity<Void> deleteSongReview(
            @Parameter(description = "Review ID", example = "10", required = true)
            @PathVariable Long reviewId) {
        songReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update the rating and description of an existing song review",
            description = "Modifies the rating and description of a song review identified by its ID. Only the owner of the review can perform this action."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SongReviewResponse.class)
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
    @PutMapping("/{songReviewId}")
    public ResponseEntity<SongReviewResponse> updateSongReview(
            @PathVariable Long songReviewId,
            @Valid @RequestBody ReviewUpdateRequest updateRequest) {
        SongReviewResponse updated = songReviewService.updateSongReview(songReviewId, updateRequest);
        return ResponseEntity.ok(updated);
    }

}