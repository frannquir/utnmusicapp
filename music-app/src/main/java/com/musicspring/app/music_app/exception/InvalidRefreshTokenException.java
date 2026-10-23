package com.musicspring.app.music_app.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired. Please sign in again.");
    }
}
