package com.musicspring.app.music_app.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ArtistEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

    @Column(name = "spotify_id")
    private String spotifyId;

    @Column(name = "name")
    private String name;

    @Column(name = "followers")
    private Integer followers;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "spotify_link")
    private String spotifyLink;

    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY)
    private List<AlbumEntity> albums;
}
