package com.musicspring.app.music_app.exception;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class DeactivatedAccountDetails {
    private Timestamp date;
    private String message;
    private String details;
    private Long userId;

    public static DeactivatedAccountDetails from(String message, String details, Long userId) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        timestamp.setNanos(0);

        return DeactivatedAccountDetails.builder()
                .date(timestamp)
                .message(message)
                .details(details)
                .userId(userId)
                .build();

    }
}
