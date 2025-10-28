package com.musicspring.app.music_app.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for artist operations",
        requiredProperties = {"name", "spotifyId"})
public class ArtistRequest {

    @Schema(description = "Spotify ID of the artist", example = "0OdUWJ0sBjDrqHygGUXeCF")
    @NotBlank(message = "Spotify ID cannot be empty")
    private String spotifyId;

    @Schema(description = "Name of the artist", example = "Taylor Swift")
    @NotNull(message = "Artist name is required.")
    private String name;

    @Schema(description = "Number of followers the artist has on Spotify", example = "84573485")
    @Positive(message = "Number of followers must be positive.")
    private Integer followers;

    @Schema(description = "URL of the artist's image", example = "https://i.scdn.co/image/ab6761610000e5eb1b5c9ad7e921fa9a9fbb2d0e")
    @Size(max = 255, message = "Image URL must be at most 255 characters.")
    private String imageUrl;

    @Size(max = 255, message = "Spotify link must be at most 255 characters.")
    @Schema(description = "Full Spotify link to the artist", example = "https://open.spotify.com/artist/0TnOYISbd1XYRBk9myaseg")
    private String spotifyLink;

    @Size(max = 100, min = 1, message = "Limit must be between 1 and 100. ")
    @Schema(description = "Maximum number of results to return", example = "10")
    private Integer limit;

    @Min(value = 0, message = "Offset must be zero or positive.")
    @Schema(description = "Result page offset", example = "0")
    private Integer offset;

    @Schema(description = "Sort results by specific field", example = "popularity")
    private String sortBy;
}