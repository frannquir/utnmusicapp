package com.musicspring.app.music_app.security.oauth2.handlers;

import com.musicspring.app.music_app.exception.AccountBannedException;
import com.musicspring.app.music_app.exception.AccountDeactivatedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String loginUrl = frontendRedirectUri.substring(0, frontendRedirectUri.indexOf("/oauth2/redirect")) + "/login";
        String targetUrl;

        Throwable cause = exception.getCause();

        if (cause instanceof AccountBannedException) {
            targetUrl = UriComponentsBuilder.fromUriString(loginUrl)
                    .queryParam("oauthError", "banned")
                    .build().toUriString();

        } else if (cause instanceof AccountDeactivatedException) {
            Long userId = ((AccountDeactivatedException) cause).getUserId();
            targetUrl = UriComponentsBuilder.fromUriString(loginUrl)
                    .queryParam("oauthError", "deactivated")
                    .queryParam("userId", userId)
                    .build().toUriString();

        } else {
            targetUrl = UriComponentsBuilder.fromUriString(loginUrl)
                    .queryParam("oauthError", "true")
                    .build().toUriString();
        }
        response.sendRedirect(targetUrl);
    }
}