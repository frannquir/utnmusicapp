package com.musicspring.app.music_app.service;

import com.musicspring.app.music_app.model.dto.response.AlbumResponse;
import com.musicspring.app.music_app.model.dto.response.ArtistResponse;
import com.musicspring.app.music_app.model.dto.response.SongResponse;
import com.musicspring.app.music_app.model.entity.ArtistEntity;
import com.musicspring.app.music_app.model.enums.ReactionType;
import com.musicspring.app.music_app.model.mapper.AlbumMapper;
import com.musicspring.app.music_app.model.mapper.ArtistMapper;
import com.musicspring.app.music_app.model.mapper.SongMapper;
import com.musicspring.app.music_app.repository.AlbumRepository;
import com.musicspring.app.music_app.repository.ArtistRepository;
import com.musicspring.app.music_app.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class StatisticService {
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final SongMapper songMapper;
    private final AlbumMapper albumMapper;
    private final ArtistMapper artistMapper;
    private final ArtistRepository artistRepository;

    @Autowired
    public StatisticService(AlbumRepository albumRepository, SongRepository songRepository, SongMapper songMapper, AlbumMapper albumMapper, ArtistMapper artistMapper, ArtistRepository artistRepository) {
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.songMapper = songMapper;
        this.albumMapper = albumMapper;
        this.artistMapper = artistMapper;
        this.artistRepository = artistRepository;
    }

    public Page<SongResponse> getMostReviewedSongs(Pageable pageable) {
        return songMapper.toResponsePage(songRepository.findTopReviewedSongs(pageable));
    }

    public Page<AlbumResponse> getMostReviewedAlbums(Pageable pageable) {
        return albumMapper.toResponsePage(albumRepository.findTopReviewedAlbums(pageable));
    }

    public Page<ArtistResponse> getTopArtistsByReactionType(ReactionType reactionType, Pageable pageable) {
        return artistMapper.toResponsePage(artistRepository.findTopArtistsByReactionType(reactionType, pageable));
    }
}