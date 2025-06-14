package com.musicspring.app.music_app.model.dto.response;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistWithAlbumsResponse {
    private Long artistId;
    private String name;
    private Integer followers;
    private String imageUrl;
    private List<AlbumResponse> albums;

}
