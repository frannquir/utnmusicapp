package com.musicspring.app.music_app.spotify.mapper;

import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.dto.response.AlbumWithTracksResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.SongResponse;

import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.time.LocalDate;
import java.util.List;

@Component
public class SpotifyMapper {

    public SongResponse toSongResponse(Track spotifyTrack) {
        if (spotifyTrack == null) {
            return null;
        }

        SongResponse songResponse = new SongResponse();
        songResponse.setSongId(null);
        songResponse.setSpotifyId(spotifyTrack.getId());
        songResponse.setName(spotifyTrack.getName());
        songResponse.setDurationMs(spotifyTrack.getDurationMs());

        if (spotifyTrack.getExternalUrls() != null) {
            songResponse.setSpotifyLink(spotifyTrack.getExternalUrls().get("spotify"));
        }

        songResponse.setPreviewUrl(spotifyTrack.getPreviewUrl());

        ArtistSimplified[] artists = spotifyTrack.getArtists();
        if (artists != null && artists.length > 0) {
            songResponse.setArtistName(artists[0].getName());
            songResponse.setArtistSpotifyId(artists[0].getId());
        }

        AlbumSimplified album = spotifyTrack.getAlbum();
        if (album != null) {
            songResponse.setAlbumName(album.getName());
            songResponse.setAlbumSpotifyId(album.getId());

            Image[] images = album.getImages();
            if (images != null && images.length > 0) {
                songResponse.setImageUrl(images[0].getUrl());
            }

            LocalDate releaseDate = parseReleaseDate(album.getReleaseDate());
            if (releaseDate != null) {
                songResponse.setReleaseDate(releaseDate);
            }
        }

        return songResponse;
    }

    public SongResponse toSongResponse(TrackSimplified trackSimplified,
                                       String albumName, String albumSpotifyId, String imageUrl,
                                       LocalDate releaseDate, String artistName, String artistSpotifyId) {
        if (trackSimplified == null) {
            return null;
        }

        // Construye la respuesta usando la info del TrackSimplified y la info del álbum pasada
        return SongResponse.builder()
                .songId(null) // No hay ID de BD aquí
                .spotifyId(trackSimplified.getId())
                .name(trackSimplified.getName())
                .durationMs(trackSimplified.getDurationMs())
                .previewUrl(trackSimplified.getPreviewUrl())
                .spotifyLink(trackSimplified.getExternalUrls() != null ? trackSimplified.getExternalUrls().get("spotify") : null)
                // Usar info del artista del trackSimplified si está disponible, sino la del álbum
                .artistName(trackSimplified.getArtists() != null && trackSimplified.getArtists().length > 0 ? trackSimplified.getArtists()[0].getName() : artistName)
                .artistSpotifyId(trackSimplified.getArtists() != null && trackSimplified.getArtists().length > 0 ? trackSimplified.getArtists()[0].getId() : artistSpotifyId)
                // Usa la info del álbum pasada como argumento
                .albumName(albumName)
                .albumSpotifyId(albumSpotifyId)
                .imageUrl(imageUrl) // Usa la imagen del álbum pasada
                .releaseDate(releaseDate) // Usa la fecha del álbum pasada
                .build();
    }

    public ArtistResponse toArtistResponse(Artist spotifyArtist) {
        if (spotifyArtist == null) {
            return null;
        }

        ArtistResponse artistResponse = new ArtistResponse();
        artistResponse.setArtistId(null);
        artistResponse.setSpotifyId(spotifyArtist.getId());
        artistResponse.setName(spotifyArtist.getName());
        artistResponse.setSpotifyLink(spotifyArtist.getExternalUrls().get("spotify"));
        artistResponse.setFollowers(spotifyArtist.getFollowers().getTotal());

        Image[] images = spotifyArtist.getImages();
        if (images != null && images.length > 0) {
            artistResponse.setImageUrl(images[0].getUrl());
        }

        return artistResponse;
    }

    public AlbumResponse toAlbumResponse(AlbumSimplified spotifyAlbum) {
        if (spotifyAlbum == null) {
            return null;
        }

        AlbumResponse albumResponse = new AlbumResponse();
        albumResponse.setAlbumId(null);
        albumResponse.setSpotifyId(spotifyAlbum.getId());
        albumResponse.setTitle(spotifyAlbum.getName());

        ArtistSimplified[] artists = spotifyAlbum.getArtists();
        if (artists != null && artists.length > 0) {
            albumResponse.setArtistName(artists[0].getName());
            albumResponse.setArtistSpotifyId(artists[0].getId());
        }

        Image[] images = spotifyAlbum.getImages();
        if (images != null && images.length > 0) {
            albumResponse.setImageUrl(images[0].getUrl());
        }

        if (spotifyAlbum.getExternalUrls() != null) {
            albumResponse.setSpotifyLink(spotifyAlbum.getExternalUrls().get("spotify"));
        }

        LocalDate releaseDate = parseReleaseDate(spotifyAlbum.getReleaseDate());
        if (releaseDate != null) {
            albumResponse.setReleaseDate(releaseDate);
        }

        return albumResponse;
    }

    public AlbumResponse toAlbumResponse(Album spotifyAlbum) {
        if (spotifyAlbum == null) {
            return null;
        }

        AlbumResponse albumResponse = new AlbumResponse();
        albumResponse.setAlbumId(null);
        albumResponse.setSpotifyId(spotifyAlbum.getId());
        albumResponse.setTitle(spotifyAlbum.getName());

        ArtistSimplified[] artists = spotifyAlbum.getArtists();
        if (artists != null && artists.length > 0) {
            albumResponse.setArtistName(artists[0].getName());
            albumResponse.setArtistSpotifyId(artists[0].getId());
        }

        Image[] images = spotifyAlbum.getImages();
        if (images != null && images.length > 0) {
            albumResponse.setImageUrl(images[0].getUrl());
        }

        if (spotifyAlbum.getExternalUrls() != null) {
            albumResponse.setSpotifyLink(spotifyAlbum.getExternalUrls().get("spotify"));
        }

        LocalDate releaseDate = parseReleaseDate(spotifyAlbum.getReleaseDate());
        if (releaseDate != null) {
            albumResponse.setReleaseDate(releaseDate);
        }

        return albumResponse;
    }

    public AlbumWithTracksResponse toAlbumWithTracksResponse(Album spotifyAlbum, List<SongResponse> trackResponses) {
        if (spotifyAlbum == null) {
            return null;
        }

        return AlbumWithTracksResponse.builder()
                .albumId(null)
                .spotifyId(spotifyAlbum.getId())
                .title(spotifyAlbum.getName())
                .artistName(spotifyAlbum.getArtists() != null && spotifyAlbum.getArtists().length > 0 ? spotifyAlbum.getArtists()[0].getName() : "Unknown Artist")
                .artistSpotifyId(spotifyAlbum.getArtists() != null && spotifyAlbum.getArtists().length > 0 ? spotifyAlbum.getArtists()[0].getId() : null)
                .imageUrl(spotifyAlbum.getImages() != null && spotifyAlbum.getImages().length > 0 ? spotifyAlbum.getImages()[0].getUrl() : null)
                .spotifyLink(spotifyAlbum.getExternalUrls() != null ? spotifyAlbum.getExternalUrls().get("spotify") : null)
                .releaseDate(parseReleaseDate(spotifyAlbum.getReleaseDate())) // Llama al método local
                .songs(trackResponses)
                .build();
    }

    public LocalDate parseReleaseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        if (dateStr.length() == 4) {
            return LocalDate.of(Integer.parseInt(dateStr), 1, 1);
        } else if (dateStr.length() == 7) {
            return LocalDate.parse(dateStr + "-01");
        } else {
            return LocalDate.parse(dateStr);
        }
    }
}