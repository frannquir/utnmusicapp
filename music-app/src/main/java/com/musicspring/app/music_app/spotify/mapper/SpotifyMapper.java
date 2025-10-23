package com.musicspring.app.music_app.spotify.mapper;

import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.SongResponse;

import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;
import java.time.LocalDate;

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

    public ArtistResponse toArtistResponse(Artist spotifyArtist) {
        if (spotifyArtist == null) {
            return null;
        }

        ArtistResponse artistResponse = new ArtistResponse();
        artistResponse.setArtistId(null);
        artistResponse.setSpotifyId(spotifyArtist.getId());
        artistResponse.setName(spotifyArtist.getName());
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

    private LocalDate parseReleaseDate(String dateStr) {
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