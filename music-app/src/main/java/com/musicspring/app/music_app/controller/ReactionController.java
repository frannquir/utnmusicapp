package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.response.ReactionResponse;
import com.musicspring.app.music_app.model.enums.ReactedType;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.service.ReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@Tag(name = "Reactions", description = "Endpoints related to reactions on reviews and comments")
public class ReactionController {

    private final ReactionService reactionService;

    @Autowired
    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @Operation(
            summary = "Get all reactions",
            description = "Retrieve a paginated list of all reactions, with optional sorting."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Reactions retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
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
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @GetMapping("/reactions")
    public ResponseEntity<Page<ReactionResponse>> getAllReactions(
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,

            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,

            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam String sort
    ) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<ReactionResponse> reactions = reactionService.findAll(pageable);
        return ResponseEntity.ok(reactions);
    }


    @Operation(
            summary = "Get reaction by its ID",
            description = "Retrieve a single reaction using its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Reaction retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReactionResponse.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Reaction not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @GetMapping("/reactions/{id}")
    public ResponseEntity<ReactionResponse> getReactionById(
            @Parameter(description = "Unique ID of the reaction", example = "1")
            @PathVariable Long id) {
        ReactionResponse reaction = reactionService.findById(id);
        return ResponseEntity.ok(reaction);
    }


    @Operation(
            summary = "Get reactions for a specific review",
            description = "Returns all reactions associated with the specified review."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Reactions retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid review ID",
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
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @GetMapping("/reviews/{reviewId}/reactions")
    public ResponseEntity<Page<ReactionResponse>> getReactionsByReview(
            @Parameter(description = "Review ID", example = "10")
            @PathVariable Long reviewId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<ReactionResponse> reactions = reactionService.findByReviewId(reviewId, pageable);
        return ResponseEntity.ok(reactions);
    }

    @Operation(
            summary = "Get reactions by comment ID",
            description = "Returns a paginated list of all reactions on a specific comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Reactions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid comment ID",
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
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping("/comments/{commentId}/reactions")
    public ResponseEntity<Page<ReactionResponse>> getReactionsByComment(
            @Parameter(description = "Comment ID", example = "15")
            @PathVariable Long commentId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<ReactionResponse> reactions = reactionService.findByCommentId(commentId, pageable);
        return ResponseEntity.ok(reactions);
    }


    @Operation(
            summary = "Get reactions filtered by type and target",
            description = "Returns a paginated list of reactions filtered by reaction type (like, love, etc.) and reacted target (review or comment)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Reactions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid parameters",
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
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping("/reactions/filter")
    public ResponseEntity<Page<ReactionResponse>> getReactionsByTypeAndTarget(
            @Parameter(description = "Reaction type", example = "LIKE")
            @RequestParam ReactionType reactionType,
            @Parameter(description = "Reacted type (REVIEW or COMMENT)", example = "REVIEW")
            @RequestParam ReactedType reactedType,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<ReactionResponse> reactions = reactionService.findReactionsByTypeAndTarget(reactionType, reactedType, pageable);
        return ResponseEntity.ok(reactions);
    }

    @Operation(
            summary = "Get reactions by user ID",
            description = "Returns a paginated list of all reactions made by a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Reactions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid user ID",
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
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @GetMapping("/users/{userId}/reactions")
    public ResponseEntity<Page<ReactionResponse>> getReactionsByUserId(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort
    ) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<ReactionResponse> reactions = reactionService.findReactionsByUserId(userId, pageable);
        return ResponseEntity.ok(reactions);
    }

    @Operation(
            summary = "Create a reaction to a review",
            description = "Creates a new reaction associated to the specified review."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Reaction created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReactionResponse.class))
            ),
            @ApiResponse(responseCode = "200",
                    description = "Reaction processed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReactionResponse.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid reaction data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Review not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PostMapping("/reviews/{reviewId}/reactions")
    public ResponseEntity<ReactionResponse> createReactionToReview(
            @Parameter(description = "Reaction type (e.g., LIKE, LOVE, etc.)", example = "LIKE", required = true)
            @RequestParam ReactionType reactionType,
            @Parameter(description = "Id of the user",example = "1", required = true)
            @RequestParam Long userId,
            @PathVariable Long reviewId
    ) {
        ReactionResponse response = reactionService.createReviewReaction(reactionType,userId, reviewId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Update a reaction",
            description = "Updates the type of an existing reaction identified by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Reaction updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReactionResponse.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid reaction type provided",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Reaction not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "403",
                    description = "User not authorized to update this reaction",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PatchMapping("/reactions/{reactionId}")
    public ResponseEntity<ReactionResponse> updateReaction(
            @PathVariable Long reactionId,
            @RequestParam ReactionType newReactionType) {

        ReactionResponse response = reactionService.updateReaction(reactionId, newReactionType);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a reaction on a review",
            description = "Permanently deletes the reaction associated with the specified review."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Reaction deleted successfully. No content is returned."
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "403",
                    description = "Access denied: insufficient privileges to delete this reaction.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Reaction or review not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @DeleteMapping("/reviews/{reviewId}/reactions/{reactionId}")
    public ResponseEntity<Void> deleteReactionToReview(
            @Parameter(description = "Review ID", example = "10", required = true)
            @PathVariable Long reviewId,
            @Parameter(description = "Reaction ID", example = "1", required = true)
            @PathVariable Long reactionId) {

        reactionService.deleteReaction(reactionId);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Create a reaction to a comment",
            description = "Creates a new reaction associated to the specified comment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Reaction created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReactionResponse.class))
            ),
            @ApiResponse(responseCode = "200",
                    description = "Reaction processed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReactionResponse.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid reaction data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Comment not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @PostMapping("/comments/{commentId}/reactions")
    public ResponseEntity<ReactionResponse> createReactionToComment(
            @Parameter(description = "Reaction type (e.g., LIKE, LOVE, etc.)", example = "LIKE", required = true)
            @RequestParam ReactionType reactionType,
            @Parameter(description = "Id of the user",example = "1", required = true)
            @RequestParam Long userId,
            @PathVariable Long commentId
    ) {
        ReactionResponse response = reactionService.createCommentReaction(reactionType,userId,commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Delete a reaction on a comment",
            description = "Permanently deletes the reaction associated with the specified comment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Reaction deleted successfully. No content is returned."
            ),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "403",
                    description = "Access denied: insufficient privileges to delete this reaction.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Reaction or comment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @DeleteMapping("/comments/{commentId}/reactions/{reactionId}")
    public ResponseEntity<Void> deleteReactionToComment(
            @Parameter(description = "Comment ID", example = "15", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "Reaction ID", example = "1", required = true)
            @PathVariable Long reactionId) {

        reactionService.deleteReaction(reactionId);
        return ResponseEntity.noContent().build();
    }


}
