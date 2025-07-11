package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.model.dto.request.SongReviewRequest;
import com.musicspring.app.music_app.model.dto.response.SongReviewResponse;
import com.musicspring.app.music_app.model.entity.SongReviewEntity;
import com.musicspring.app.music_app.model.entity.SongEntity;
import com.musicspring.app.music_app.model.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SongReviewMapper {

    private final UserMapper userMapper;
    private final SongMapper songMapper;

    @Autowired
    public SongReviewMapper(UserMapper userMapper, SongMapper songMapper) {
        this.userMapper = userMapper;
        this.songMapper = songMapper;
    }

    public SongReviewResponse toResponse(SongReviewEntity songReview) {
        return SongReviewResponse.builder()
                .songReviewId(songReview.getReviewId())
                .rating(songReview.getRating())
                .description(songReview.getDescription())
                .date(songReview.getDate())
                .active(songReview.getActive())
                .user(userMapper.toResponse(songReview.getUser()))
                .song(songMapper.toResponse(songReview.getSong()))
                .build();
    }

    public List<SongReviewResponse> toResponseList(List<SongReviewEntity> songReviews) {
        return songReviews.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<SongReviewResponse> toResponsePage(Page<SongReviewEntity> songReviewPage) {
        return songReviewPage.map(this::toResponse);
    }

    public SongReviewEntity toEntity(SongReviewRequest songReviewRequest, UserEntity userEntity, SongEntity songEntity) {
        return SongReviewEntity.builder()
                .active(true)
                .description(songReviewRequest.getDescription())
                .rating(songReviewRequest.getRating())
                .song(songEntity)
                .user(userEntity)
                .build();
    }
}
