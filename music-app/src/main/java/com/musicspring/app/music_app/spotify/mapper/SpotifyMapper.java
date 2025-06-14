package com.musicspring.app.music_app.spotify.mapper;

import com.musicspring.app.music_app.model.dto.request.AlbumRequest;
import com.musicspring.app.music_app.model.dto.request.ArtistRequest;
import com.musicspring.app.music_app.model.dto.request.SongRequest;

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

    public AlbumRequest toAlbumRequest(AlbumSimplified spotifyAlbum) {
        if (spotifyAlbum == null) {
            return null;
        }

        AlbumRequest albumRequest = new AlbumRequest();
        albumRequest.setSpotifyId(spotifyAlbum.getId());
        albumRequest.setTitle(spotifyAlbum.getName());

        ArtistSimplified[] artists = spotifyAlbum.getArtists();
        albumRequest.setArtistSpotifyId(artists[0].getId());
        // Get image URL from Spotify
        String imageUrl;
        Image[] images = spotifyAlbum.getImages();
        if (images != null && images.length > 0) {
            imageUrl = images[0].getUrl();
            albumRequest.setImageUrl(imageUrl);
        }

        if (spotifyAlbum.getExternalUrls() != null) {
            albumRequest.setSpotifyLink(spotifyAlbum.getExternalUrls().get("spotify"));
        }

        LocalDate releaseDate = parseReleaseDate(spotifyAlbum.getReleaseDate());
        if (releaseDate != null) {
            albumRequest.setReleaseDate(releaseDate);
        }
        return albumRequest;
    }

    public AlbumRequest toAlbumRequest(Album spotifyAlbum) {
        if (spotifyAlbum == null) {
            return null;
        }

        AlbumRequest albumRequest = new AlbumRequest();
        albumRequest.setSpotifyId(spotifyAlbum.getId());
        albumRequest.setTitle(spotifyAlbum.getName());

        ArtistSimplified[] artists = spotifyAlbum.getArtists();
        albumRequest.setArtistSpotifyId(artists[0].getId());

        // Get image URL from Spotify
        String imageUrl;
        Image[] images = spotifyAlbum.getImages();
        if (images != null && images.length > 0) {
            imageUrl = images[0].getUrl();
            albumRequest.setImageUrl(imageUrl);
        }

        if (spotifyAlbum.getExternalUrls() != null) {
            albumRequest.setSpotifyLink(spotifyAlbum.getExternalUrls().get("spotify"));
        }

        LocalDate releaseDate = parseReleaseDate(spotifyAlbum.getReleaseDate());
        if(releaseDate != null) {
            albumRequest.setReleaseDate(releaseDate);
        }

        return albumRequest;
    }



    public ArtistRequest toArtistRequest(Artist spotifyArtist) {
        if (spotifyArtist == null) {
            return null;
        }

        ArtistRequest artistRequest = new ArtistRequest();
        artistRequest.setSpotifyId(spotifyArtist.getId());
        artistRequest.setName(spotifyArtist.getName());
        artistRequest.setFollowers(spotifyArtist.getFollowers().getTotal());

        String imageUrl;
        Image[] images = spotifyArtist.getImages();
        if (images != null && images.length > 0) {
            imageUrl = images[0].getUrl();
            artistRequest.setImageUrl(imageUrl);
        }

        return artistRequest;
    }
    
    public SongRequest toSongRequest(Track spotifyTrack) {
        if (spotifyTrack == null) {
            return null;
        }
        
        SongRequest songRequest = new SongRequest();
        songRequest.setSpotifyId(spotifyTrack.getId());
        songRequest.setName(spotifyTrack.getName());
        songRequest.setDurationMs(spotifyTrack.getDurationMs());
        
        if (spotifyTrack.getExternalUrls() != null) {
            songRequest.setSpotifyLink(spotifyTrack.getExternalUrls().get("spotify"));
        }
        
        songRequest.setPreviewUrl(spotifyTrack.getPreviewUrl());
        
        ArtistSimplified[] artists = spotifyTrack.getArtists();
        songRequest.setArtistSpotifyId(artists[0].getId());
        
        AlbumSimplified album = spotifyTrack.getAlbum();
        if (album != null) {
            songRequest.setAlbumSpotifyId(album.getId());
            Image[] images = album.getImages();
            if (images != null && images.length > 0) {
                songRequest.setImageUrl(images[0].getUrl());
            }
            
            LocalDate releaseDate = parseReleaseDate(album.getReleaseDate());
            if (releaseDate != null) {
                songRequest.setReleaseDate(releaseDate);
            }
        }
        
        return songRequest;
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