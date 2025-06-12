package com.musicspring.app.music_app.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for song operations",
        requiredProperties = {"name", "artistName", "albumName", "spotifyId"})
public class SongRequest {

    @NotBlank(message = "Spotify ID can not be empty..")
    @Schema(description = "Spotify ID of the song", example = "7qiZfU4dY1lWllzX7mPBI3")
    private String spotifyId;

    @NotBlank(message = "Name of the song can not be empty.")
    @Schema(description = "Name of the song", example = "Shape of You")
    private String name;

    @NotBlank(message = "Artist name can not be empty.")
    @Schema(description = "Name of the artist", example = "Ed Sheeran")
    private String artistName;

    @NotBlank(message = "Album name can not be empty.")
    @Schema(description = "Name of the album", example = "÷ (Divide)")
    private String albumName;

    @Size(max = 255, message = "Image URL must be at most 255 characters.")
    @Schema(description = "URL of the album or song cover image", example = "https://i.scdn.co/image/ab67616d0000b273ba5db46f4b838ef6027e6f96")
    private String imageUrl;

    @PositiveOrZero(message = "Duration must be zero or positive.")
    @Schema(description = "Duration of the song in milliseconds", example = "233712")
    private Integer durationMs;

    @Size(max = 255, message = "Preview URL must be at most 255 characters")
    @Schema(description = "URL to a 30-second song preview", example = "https://p.scdn.co/mp3-preview/abc123")
    private String previewUrl;

    @Size(max = 255, message = "Spotify link must be at most 255 characters")
    @Schema(description = "Full Spotify link to the song", example = "https://open.spotify.com/track/7qiZfU4dY1lWllzX7mPBI3")
    private String spotifyLink;

    @NotNull(message = "Release date can not be null.")
    @PastOrPresent(message = "Release date cannot be in the future.")
    @Schema(description = "Release date of the song", example = "2017-03-03")
    private LocalDate releaseDate;

    @Size(max = 100, min = 1, message = "Limit must be between 1 and 100. ")
    @Schema(description = "Maximum number of results to return", example = "10")
    private Integer limit;

    @Min(value = 0, message = "Offset must be zero or positive.")
    @Schema(description = "Result page offset", example = "0")
    private Integer offset;

    @Schema(description = "Sort results by specific field", example = "popularity")
    private String sortBy;

    @Schema(description = "Spotify ID of the album", example = "3T4tUhGYeRNVUGevb0wThu")
    private String albumSpotifyId;

    @Schema(description = "Spotify ID of the artist", example = "6eUKZXaKkcviH0Ku9w2n3V")
    private String artistSpotifyId;
}
