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

    @OneToMany(mappedBy = "artist")
    private List<AlbumEntity> albums;
}
