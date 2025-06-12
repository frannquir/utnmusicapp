package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.request.AlbumReviewRequest;
import com.musicspring.app.music_app.model.dto.response.AlbumReviewResponse;
import com.musicspring.app.music_app.model.dto.response.SongReviewResponse;
import com.musicspring.app.music_app.model.mapper.AlbumReviewMapper;
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
    public AlbumReviewController(AlbumReviewService albumReviewService, AlbumReviewMapper albumReviewMapper) {
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
            @RequestParam(required = false) Long songId,
            @Parameter(description = "Spotify ID of the album to review (for albums from Spotify)", example = "4eLPsYPBmXABThSJ821sqY")
            @RequestParam(required = false) String spotifyId,
            @Parameter(description = "Data for the new album review", required = true)
            @Valid @RequestBody AlbumReviewRequest albumReviewRequest) {

        AlbumReviewResponse savedReview = albumReviewService.createAlbumReview(
                songId, spotifyId, albumReviewRequest);

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
            @Parameter(description = "Field to sort by", example = "date", required = true)
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<AlbumReviewResponse> albumReviewResponsePage = albumReviewService.findByUserId(userId, pageable);
        return ResponseEntity.ok(albumReviewResponsePage);
    }

    @Operation(
            summary = "Retrieve album reviews by album ID",
            description = "Fetches a paginated list of reviews for a specific album, sorted by the specified parameter."
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
    @GetMapping("/album/{albumId}")
    public ResponseEntity<Page<AlbumReviewResponse>> getAlbumReviewsByAlbumId(
            @Parameter(description = "ID of the album whose reviews are to be retrieved", example = "1", required = true)
            @PathVariable Long albumId,
            @Parameter(description = "Number of items per page", example = "10", required = true)
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0", required = true)
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "rating", required = true)
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<AlbumReviewResponse> albumReviewResponsePage = albumReviewService.findByAlbumId(albumId, pageable);
        return ResponseEntity.ok(albumReviewResponsePage);
    }

    @Operation(
            summary = "Get album reviews by Spotify ID",
            description = "Retrieves a paginated list of reviews for the specified album using its Spotify ID."
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
                    responseCode = "404",
                    description = "Album not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
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
    @GetMapping("/spotify/{spotifyId}")
    public ResponseEntity<Page<AlbumReviewResponse>> getAlbumReviewsBySpotifyId(
            @Parameter(description = "Spotify ID of the album whose reviews are requested", example = "4iV5W9uYEdYUVa79Axb7Rh")
            @PathVariable String spotifyId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,
            @Parameter(description = "Field to sort by", example = "date")
            @RequestParam String sort){
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<AlbumReviewResponse> albumReviewResponsePage = albumReviewService.findBySpotifyId(spotifyId, pageable);
        return ResponseEntity.ok(albumReviewResponsePage);
    }
}

