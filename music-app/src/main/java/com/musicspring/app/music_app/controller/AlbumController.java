package com.musicspring.app.music_app.controller;

import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.service.AlbumService;
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
@RequestMapping("/api/v1/albums")
@Tag(name = "Albums", description = "Endpoints for managing albums")
public class AlbumController {
    private final AlbumService albumService;

    @Autowired
    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @Operation(summary = "Get all albums",
            description = "Retrieve a paginated list of all albums, with optional sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Albums retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping()
    public ResponseEntity<Page<AlbumResponse>> getAllAlbums(
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,

            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,

            @Parameter(description = "Field to sort by", example = "title")
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        return ResponseEntity.ok(albumService.findAll(pageable));
    }

    @Operation(
            summary = "Get album by its ID",
            description = "Retrieve a single album using its internal database ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Album retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlbumResponse.class))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Album not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            ),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbumById(
            @Parameter(description = "Internal ID of the album", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(albumService.findById(id));
    }


    @Operation(summary = "Get album by Spotify ID",
            description = "Retrieve a single album using its Spotify ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Album retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AlbumResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Album not found",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/spotify/{spotifyId}")
    public ResponseEntity<AlbumResponse> getAlbumBySpotifyId(
            @Parameter(description = "Spotify ID of the album", example = "6z4NLXyHPga1UmSJsPK7G1")
            @PathVariable String spotifyId) {
            return ResponseEntity.ok(albumService.findBySpotifyId(spotifyId));
    }

    @Operation(summary = "Search albums",
            description = "Search for albums by title or other fields, with pagination and sorting.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Albums retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid search parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<AlbumResponse>> searchAlbums(
            @Parameter(description = "Search query string", example = "nostalgia")
            @RequestParam String query,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam int size,

            @Parameter(description = "Page number to retrieve (0-based)", example = "0")
            @RequestParam int pageNumber,

            @Parameter(description = "Field to sort by", example = "releaseDate")
            @RequestParam String sort) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sort));
        return ResponseEntity.ok(albumService.searchAlbums(query, pageable));
    }

}

