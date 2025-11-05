package com.musicspring.app.music_app.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "album_reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "album_id"})
})

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class AlbumReviewEntity extends ReviewEntity {
    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private AlbumEntity album;

}