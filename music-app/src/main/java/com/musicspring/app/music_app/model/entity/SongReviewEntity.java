package com.musicspring.app.music_app.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "song_reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "song_id"})
})

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class SongReviewEntity extends ReviewEntity {

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private SongEntity song;

}