package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Administrative operations")
public class AdminController {
    private final ReviewService reviewService;

    @Autowired
    public AdminController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(
            summary = "Get all reviews (Admin)",
            description = "Retrieves a paginated list of all reviews (songs and albums) for administration purposes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reviews retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
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
                    responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden: You do not have permission to access this resource (Admin only).",
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
    @GetMapping("/reviews")
    public ResponseEntity<Page<Object>> getAllReviews(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "desc") String direction){
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(reviewService.getAllReviews(pageable));
    }

    @Operation(
            summary = "Delete a review (Admin)",
            description = "Performs a soft delete on any review regardless of ownership."
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
                    description = "Forbidden: You do not have permission to delete this review (Admin only).",
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
    @DeleteMapping("/review/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reactivate a review (Admin)",
            description = "Reactivates a previously deleted review, provided the owner user is currently active."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review reactivated successfully."),
            @ApiResponse(responseCode = "409", description = "Conflict: Review is already active or User is inactive."),
            @ApiResponse(responseCode = "404", description = "Review not found.")
    })
    @PutMapping("/review/{id}/reactivate")
    public ResponseEntity<Void> reActivateReview(@PathVariable Long id) {
        reviewService.reActivateReview(id);
        return ResponseEntity.noContent().build();
    }


}
