package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistWithAlbumsResponse;
import com.musicspring.app.music_app.service.ArtistService;
import com.musicspring.app.music_app.exception.ErrorDetails;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artists")
@Tag(name = "Artists", description = "Operations related to musical artists")
public class ArtistController {

    private final ArtistService artistService;

    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @Operation(
            summary = "Get all artists",
            description = "Retrieve a paginated list of all artists, with optional sorting."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Artists retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<ArtistResponse>> getAllArtists(
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,

            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,

            @Parameter(description = "Field to sort by", example = "name")
            @RequestParam String sort
    ) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        Page<ArtistResponse> response = artistService.getAllArtists(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get artist by its ID",
            description = "Retrieve a single artist using its internal database ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Artist retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArtistResponse.class))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Artist not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getArtistById(
            @Parameter(description = "Internal ID of the artist", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistResponseById(id));
    }

    @Operation(
            summary = "Delete an artist by its ID",
            description = "Marks the artist as deleted (soft delete) corresponding to the provided ID. If the artist does not exist, a 404 response is returned."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Artist deleted successfully. No content is returned."
            ),
            @ApiResponse(responseCode = "404",
                    description = "Artist not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(
            @Parameter(description = "ID of the artist to delete", required = true, example = "1")
            @PathVariable Long id) {
        artistService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search artists by name", description = "Searches for artists matching the provided name (partial or full) with pagination support.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching artists retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid name parameter",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetails.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ArtistResponse>> searchArtistsByName(
            @Parameter(description = "Name or partial name to search for", example = "Eminem")
            @RequestParam String name,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<ArtistResponse> artistsPage = artistService.searchArtists(name, pageable);
        return ResponseEntity.ok(artistsPage);
    }

}
