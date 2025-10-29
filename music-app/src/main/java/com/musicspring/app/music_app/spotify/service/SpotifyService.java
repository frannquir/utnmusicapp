package com.musicspring.app.music_app.spotify.service;

import com.musicspring.app.music_app.exception.SpotifyServiceException;
import com.musicspring.app.music_app.model.dto.response.*;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
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
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchArtistsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final SpotifyConfig spotifyConfig;
    private final SpotifyMapper spotifyMapper;
    private final ArtistMapper artistMapper;

    @Value("${spotify.default.limit:20}")
    private int defaultLimit;

    private void checkTokenExpiration() {
        if (LocalDateTime.now().isAfter(spotifyConfig.getTokenExpiration())) {
            spotifyConfig.refreshToken(spotifyApi);
        }
    }

    public Page<AlbumResponse> searchAlbums(String query, Pageable pageable) {
        checkTokenExpiration();

        try {
            SearchAlbumsRequest request = spotifyApi.searchAlbums(query)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();

            Paging<AlbumSimplified> results = request.execute();

            List<AlbumResponse> albumResponses = Arrays.stream(results.getItems())
                    .map(spotifyMapper::toAlbumResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(albumResponses, pageable, results.getTotal());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error searching albums", e);
        }
    }



    public Page<ArtistResponse> searchArtists(String query, Pageable pageable) {
        checkTokenExpiration();

        try {
            SearchArtistsRequest request = spotifyApi.searchArtists(query)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();

            Paging<Artist> results = request.execute();

            List<ArtistResponse> artistResponses = Arrays.stream(results.getItems())
                    .map(spotifyMapper::toArtistResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(artistResponses, pageable, results.getTotal());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error searching artists", e);
        }
    }



    public Page<SongResponse> searchSongs(String query, Pageable pageable) {
        checkTokenExpiration();

        try {
            SearchTracksRequest request = spotifyApi.searchTracks(query)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();

            Paging<Track> results = request.execute();

            List<SongResponse> songResponses = Arrays.stream(results.getItems())
                    .map(spotifyMapper::toSongResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(songResponses, pageable, results.getTotal());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error searching songs", e);
        }
    }

    public AlbumWithTracksResponse getAlbum(String albumId) {
        checkTokenExpiration();
        try {
            GetAlbumRequest request = spotifyApi.getAlbum(albumId).build();
            Album spotifyAlbum = request.execute();

            if (spotifyAlbum == null){
                throw new SpotifyServiceException("Error obtaining album");
            }

            GetAlbumsTracksRequest tracksRequest = spotifyApi.getAlbumsTracks(albumId)
                    .limit(50)
                    .offset(0)
                    .build();
            Paging<TrackSimplified> tracksPaging = tracksRequest.execute();
            List<SongResponse> trackResponses = Collections.emptyList();

            if (tracksPaging != null && tracksPaging.getItems() != null) {
                String albumName = spotifyAlbum.getName();
                String albumSpotifyId = spotifyAlbum.getId();
                String imageUrl = (spotifyAlbum.getImages() != null && spotifyAlbum.getImages().length > 0) ? spotifyAlbum.getImages()[0].getUrl() : null;
                LocalDate releaseDate = spotifyMapper.parseReleaseDate(spotifyAlbum.getReleaseDate());
                String artistName = (spotifyAlbum.getArtists() != null && spotifyAlbum.getArtists().length > 0) ? spotifyAlbum.getArtists()[0].getName() : "Unknown Artist";
                String artistSpotifyId = (spotifyAlbum.getArtists() != null && spotifyAlbum.getArtists().length > 0) ? spotifyAlbum.getArtists()[0].getId() : null;

                trackResponses = Arrays.stream(tracksPaging.getItems())
                        .map(trackSimplified -> spotifyMapper.toSongResponse(trackSimplified, albumName, albumSpotifyId, imageUrl, releaseDate, artistName, artistSpotifyId))
                        .collect(Collectors.toList());
            }

            return spotifyMapper.toAlbumWithTracksResponse(spotifyAlbum, trackResponses);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error obtaining album", e);
        }
    }

    public ArtistWithAlbumsResponse getArtist(String artistId) {
        checkTokenExpiration();

        try {
            GetArtistRequest request = spotifyApi.getArtist(artistId).build();
            se.michaelthelin.spotify.model_objects.specification.Artist spotifyArtist = request.execute();
            Artist spotifyArt = request.execute();
            if(spotifyArt==null){
                throw new SpotifyServiceException("Error obtaining artist");
            }

            GetArtistsAlbumsRequest albumsRequest = spotifyApi.getArtistsAlbums(artistId)
                    .limit(40)
                    .offset(0)
                    .build();
            Paging<AlbumSimplified> albumSimplifiedPaging = albumsRequest.execute();

            List<AlbumResponse> albumResponses = Collections.emptyList();

            if (albumSimplifiedPaging!=null && albumSimplifiedPaging.getItems()!= null){
                albumResponses=Arrays.stream(albumSimplifiedPaging.getItems())
                        .map(spotifyMapper::toAlbumResponse)
                        .collect(Collectors.toList());
            }

            return artistMapper.spotifyArtistToArtistWithAlbumesResponse(spotifyArtist,albumResponses);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error obtaining artist", e);
        }
    }

    public SongResponse getSong(String trackId) {
        checkTokenExpiration();

        try {
            GetTrackRequest request = spotifyApi.getTrack(trackId).build();
            Track spotifyTrack = request.execute();

            return spotifyMapper.toSongResponse(spotifyTrack);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyServiceException("Error obtaining song", e);
        }
    }

    public UnifiedSearchResponse searchAll(String query, Pageable pageable) {
        UnifiedSearchResponse response = new UnifiedSearchResponse();
        response.setQuery(query);

        Page<SongResponse> songResults = searchSongs(query, pageable);
        Page<ArtistResponse> artistResults = searchArtists(query, pageable);
        Page<AlbumResponse> albumResults = searchAlbums(query, pageable);

        response.setSongs(songResults);
        response.setArtists(artistResults);
        response.setAlbums(albumResults);

        return response;
    }
}