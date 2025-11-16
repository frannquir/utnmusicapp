package com.musicspring.app.music_app.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.HashMap;
import java.util.Map;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorDetails.from(ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        String errorMessage = "Validation failed: " + errors;
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDetails.from(errorMessage, request.getDescription(false)));
    }

    @ExceptionHandler(SpotifyServiceException.class)
    public ResponseEntity<ErrorDetails> handleSpotifyServiceException(SpotifyServiceException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorDetails.from("Spotify resource not found", request.getDescription(false)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        System.err.println("Unexpected error: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDetails.from("Internal server error: " + ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorDetails> handleHttpClientError(HttpClientErrorException ex, WebRequest request) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ErrorDetails.from("Spotify API error: " + ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorDetails> handleResourceAccessException(WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorDetails.from("Could not connect to Spotify service", request.getDescription(false)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDetails.from(ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetails> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDetails.from(ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorDetails.from("Invalid credentials.", request.getDescription(false)));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorDetails.from(ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<DeactivatedAccountDetails> handleAccountDeactivated(
            AccountDeactivatedException ex, WebRequest request, HttpServletResponse response) throws IOException { // <-- Añade HttpServletResponse
        if (request.getDescription(false).contains("/login/oauth2/code/")) {
            String loginUrl = frontendRedirectUri.substring(0, frontendRedirectUri.indexOf("/oauth2/redirect")) + "/login";
            String targetUrl = UriComponentsBuilder.fromUriString(loginUrl)
                    .queryParam("oauthError", "deactivated")
                    .queryParam("userId", ex.getUserId())
                    .build().toUriString();
            response.sendRedirect(targetUrl);
            return null;
        }
        DeactivatedAccountDetails details = DeactivatedAccountDetails.from(
                ex.getMessage(),
                request.getDescription(false),
                ex.getUserId()
        );
        return new ResponseEntity<>(details, HttpStatus.LOCKED);
    }

    @ExceptionHandler(AccountBannedException.class)
    public ResponseEntity<ErrorDetails> handleAccountBanned(
            AccountBannedException ex, WebRequest request, HttpServletResponse response) throws IOException { // <-- Añade HttpServletResponse
        if (request.getDescription(false).contains("/login/oauth2/code/")) {
            String loginUrl = frontendRedirectUri.substring(0, frontendRedirectUri.indexOf("/oauth2/redirect")) + "/login";
            String targetUrl = UriComponentsBuilder.fromUriString(loginUrl)
                    .queryParam("oauthError", "banned")
                    .build().toUriString();

            response.sendRedirect(targetUrl);
            return null;
        }
        ErrorDetails details = ErrorDetails.from(
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(details, HttpStatus.LOCKED);
    }
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateReview(DuplicateReviewException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorDetails.from(ex.getMessage(), request.getDescription(false)));
    }

}