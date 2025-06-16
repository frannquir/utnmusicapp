package com.musicspring.app.music_app.exception;

/**
 * Spotify has checked exceptions that can't be handled by GlobalExceptionHandler directly.
 * This converts them to unchecked exceptions for centralized error handling.
 */
public class SpotifyServiceException extends RuntimeException {
    
    public SpotifyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SpotifyServiceException(String message) {
        super(message);
    }
}
