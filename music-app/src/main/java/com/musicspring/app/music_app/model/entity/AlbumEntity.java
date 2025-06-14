package com.musicspring.app.music_app.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "albums")

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AlbumEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "spotify_id")
    private String spotifyId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "spotify_link")
    private String spotifyLink;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistEntity artist;

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    private List<SongEntity> songs;

}
