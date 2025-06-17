package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.exception.ErrorDetails;
import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.SongResponse;
import com.musicspring.app.music_app.model.entity.ArtistEntity;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.service.StatisticService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/v1/stats")
@Tag(name = "Stats", description = "Endpoints related to stats for songs, albums and artists")
public class StatisticController {

    private final StatisticService statisticService;

    @Autowired
    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @Operation(
            summary = "Retrieve the most reviewed songs",
            description = "Fetches a paginated list of the most reviewed songs, sorted by the number of reviews in descending order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Top reviewed songs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
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
    @GetMapping("/songs/mostReviewed")
    public ResponseEntity<Page<SongResponse>> geMostReviewedSongs(
            @Parameter(description = "Number of items per page", example = "3")
            @RequestParam(defaultValue = "3") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, size);
        return ResponseEntity.ok(statisticService.getMostReviewedSongs(pageable));
    }

    @Operation(
            summary = "Get top songs by reaction type",
            description = "Retrieve a paginated list of songs ranked by the total number of reactions of a specific type (e.g., LIKE, LOVE, etc.)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of songs",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or request parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/songs/mostReacted")
    public ResponseEntity<Page<SongResponse>> geMostReactedSongs(
            @Parameter(description = "Reaction type to filter by (e.g., LIKE, LOVE, etc.)", example = "LIKE", required = true)
            @RequestParam ReactionType reactionType,
            @Parameter(description = "Number of items per page", example = "3")
            @RequestParam(defaultValue = "3") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber, size);
        return ResponseEntity.ok(statisticService.getTopSongsByReactionType(reactionType, pageable));
    }

    @Operation(
            summary = "Retrieve the most reviewed albums",
            description = "Fetches a paginated list of the most reviewed albums, sorted by the number of reviews in descending order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Top reviewed albums retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
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
    @GetMapping("/albums/mostReviewed")
    public ResponseEntity<Page<AlbumResponse>> getMostReviewedAlbums(
            @Parameter(description = "Number of items per page", example = "3")
            @RequestParam(defaultValue = "3") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, size);
        return ResponseEntity.ok(statisticService.getMostReviewedAlbums(pageable));
    }


    @Operation(
            summary = "Get top albums by reaction type",
            description = "Retrieve a paginated list of albums ranked by the total number of reactions of a specific type (e.g., LIKE, LOVE, etc.)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of albums",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or request parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/albums/mostReacted")
    public ResponseEntity<Page<AlbumResponse>> getMostReactedAlbums(
            @Parameter(description = "Reaction type to filter by (e.g., LIKE, LOVE, etc.)", example = "LIKE", required = true)
            @RequestParam ReactionType reactionType,
            @Parameter(description = "Number of items per page", example = "3")
            @RequestParam(defaultValue = "3") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber, size);
        return ResponseEntity.ok(statisticService.getTopAlbumsByReactionType(reactionType, pageable));
    }

    @Operation(
            summary = "Get top artists by reaction type",
            description = "Retrieve a paginated list of artists ranked by the total number of reactions of a specific type (e.g., LIKE, LOVE, etc.) across all their albums and songs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of artists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or request parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/artists/mostReacted")
    public ResponseEntity<Page<ArtistResponse>> getGetMostReactedArtistsByReactionType(
            @Parameter(description = "Reaction type to filter by (e.g., LIKE, LOVE, etc.)", example = "LIKE", required = true)
            @RequestParam ReactionType reactionType,
            @Parameter(description = "Number of items per page", example = "3")
            @RequestParam(defaultValue = "3") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, size);
        return ResponseEntity.ok(statisticService.getTopArtistsByReactionType(reactionType, pageable));
    }


    @Operation(
            summary = "Get most reviewed artists",
            description = "Retrieve a paginated list of artists ranked by the total number of reviews across all their albums and songs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of artists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or request parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401",
                    description = "Authentication is required to access this resource.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/artists/mostReviewed")
    public ResponseEntity<Page<ArtistResponse>> getMostReviewedArtists(
            @Parameter(description = "Number of items per page", example = "3")
            @RequestParam(defaultValue = "3") int size,
            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber, size);
        return ResponseEntity.ok(statisticService.getMostReviewedArtists(pageable));
    }
}