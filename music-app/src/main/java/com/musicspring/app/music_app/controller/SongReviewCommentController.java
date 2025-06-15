package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.request.CommentPatchRequest;
import com.musicspring.app.music_app.model.dto.request.CommentRequest;
import com.musicspring.app.music_app.model.dto.response.CommentResponse;
import com.musicspring.app.music_app.model.enums.CommentType;
import com.musicspring.app.music_app.service.CommentService;
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
@RequestMapping("api/v1/songreviews/{reviewId}/comments")
@Tag(name = "Song Review Comments", description = "Manage comments on song reviews")
public class SongReviewCommentController {

    private final CommentService commentService;

    @Autowired
    public SongReviewCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(
            summary = "Create a new comment for a song or album review",
            description = "Adds a new comment associated with the specified song or album review."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponse.class)
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
                    responseCode = "404",
                    description = "Review or User not found",
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
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long reviewId,
            @Valid @RequestBody CommentRequest commentRequest) {

        CommentResponse createdComment = commentService.createSongReviewComment(commentRequest, reviewId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @Operation(
            summary = "Update the content of an existing comment",
            description = "Modifies the content of a comment identified by its ID. Only the owner of the comment can perform this action."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponse.class)
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
                    description = "Access denied - user does not own the comment",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found",
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
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateCommentContent(
            @PathVariable Long commentId,
            @RequestBody CommentPatchRequest patchRequest) {
        CommentResponse updated = commentService.updateCommentContent(commentId, patchRequest);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Retrieve comments for a specific song review",
            description = "Fetches a paginated list of comments for the given song review ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
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
                    responseCode = "400",
                    description = "Invalid parameters",
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
    public ResponseEntity<Page<CommentResponse>> getCommentsByReviewId(
            @Parameter(description = "ID of the song review", example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort) {

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<CommentResponse> comments = commentService.getCommentsByReviewId(reviewId, pageable);
        return ResponseEntity.ok(comments);
    }

    @Operation(
            summary = "Get comment by ID",
            description = "Retrieve a comment by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @Parameter(description = "ID of the comment", example = "1")
            @PathVariable Long commentId) {

        CommentResponse comment = commentService.findById(commentId);
        return ResponseEntity.ok(comment);
    }

    @Operation(
            summary = "Delete comment by ID",
            description = "Deletes a comment by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteCommentById(
            @Parameter(description = "ID of the comment", example = "1")
            @PathVariable Long commentId) {

        commentService.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Retrieve comments by user ID",
            description = "Fetches a paginated list of comments made by a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsByUserId(
            @Parameter(description = "ID of the user", example = "1")
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "createdAt") String sort) {

        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<CommentResponse> comments = commentService.findByUserId(userId, pageable);
        return ResponseEntity.ok(comments);
    }

}
