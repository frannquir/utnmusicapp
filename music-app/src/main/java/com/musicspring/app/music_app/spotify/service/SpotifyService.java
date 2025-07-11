package com.musicspring.app.music_app.spotify.service;

import com.musicspring.app.music_app.exception.SpotifyServiceException;
import com.musicspring.app.music_app.model.dto.request.AlbumRequest;
import com.musicspring.app.music_app.model.dto.request.ArtistRequest;
import com.musicspring.app.music_app.model.dto.request.SongRequest;
import com.musicspring.app.music_app.spotify.config.SpotifyConfig;
import com.musicspring.app.music_app.spotify.mapper.SpotifyMapper;
import com.musicspring.app.music_app.spotify.model.UnifiedSearchResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchArtistsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final SpotifyConfig spotifyConfig;
    private final SpotifyMapper spotifyMapper;

    @Value("${spotify.default.limit:20}")
    private int defaultLimit;

    private void checkTokenExpiration() {
        if (LocalDateTime.now().isAfter(spotifyConfig.getTokenExpiration())) {
            spotifyConfig.refreshToken(spotifyApi);
        }
    }

    public Page<AlbumRequest> searchAlbums(String query, Pageable pageable) {
        checkTokenExpiration();

        try {
            SearchAlbumsRequest request = spotifyApi.searchAlbums(query)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();

            Paging<AlbumSimplified> results = request.execute();

            List<AlbumRequest> albumRequests = Arrays.stream(results.getItems())
                    .map(spotifyMapper::toAlbumRequest)
                    .collect(Collectors.toList());

            return new PageImpl<>(albumRequests, pageable, results.getTotal());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error searching albums", e);
        }
    }



    public Page<ArtistRequest> searchArtists(String query, Pageable pageable) {
        checkTokenExpiration();

        try {
            SearchArtistsRequest request = spotifyApi.searchArtists(query)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();

            Paging<Artist> results = request.execute();

            List<ArtistRequest> artistRequests = Arrays.stream(results.getItems())
                    .map(spotifyMapper::toArtistRequest)
                    .collect(Collectors.toList());

            return new PageImpl<>(artistRequests, pageable, results.getTotal());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error searching artists", e);
        }
    }



    public Page<SongRequest> searchSongs(String query, Pageable pageable) {
        checkTokenExpiration();

        try {
            SearchTracksRequest request = spotifyApi.searchTracks(query)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();

            Paging<Track> results = request.execute();

            List<SongRequest> songRequests = Arrays.stream(results.getItems())
                    .map(spotifyMapper::toSongRequest)
                    .collect(Collectors.toList());

            return new PageImpl<>(songRequests, pageable, results.getTotal());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error searching songs", e);
        }
    }



    public AlbumRequest getAlbum(String albumId) {
        checkTokenExpiration();

        try {
            GetAlbumRequest request = spotifyApi.getAlbum(albumId).build();
            se.michaelthelin.spotify.model_objects.specification.Album spotifyAlbum = request.execute();

            return spotifyMapper.toAlbumRequest(spotifyAlbum);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error obtaining album", e);
        }
    }

    public ArtistRequest getArtist(String artistId) {
        checkTokenExpiration();

        try {
            GetArtistRequest request = spotifyApi.getArtist(artistId).build();
            se.michaelthelin.spotify.model_objects.specification.Artist spotifyArtist = request.execute();

            return spotifyMapper.toArtistRequest(spotifyArtist);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error obtaining artist", e);
        }
    }

    public SongRequest getSong(String trackId) {
        checkTokenExpiration();

        try {
            GetTrackRequest request = spotifyApi.getTrack(trackId).build();
            Track spotifyTrack = request.execute();

            return spotifyMapper.toSongRequest(spotifyTrack);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error obtaining song", e);
        }
    }

    public UnifiedSearchResponse searchAll(String query, Pageable pageable) {
        UnifiedSearchResponse response = new UnifiedSearchResponse();
        response.setQuery(query);

        Page<SongRequest> songResults = searchSongs(query, pageable);
        Page<ArtistRequest> artistResults = searchArtists(query, pageable);
        Page<AlbumRequest> albumResults = searchAlbums(query, pageable);
        
        response.setSongs(songResults);
        response.setArtists(artistResults);
        response.setAlbums(albumResults);
        
        return response;
    }
}