package com.musicspring.app.music_app.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration(proxyBeanMethods = false)
public class EnviromentConfig {

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        System.setProperty("jwt.secret", Objects.requireNonNull(dotenv.get("JWT_SECRET")));
        System.setProperty("spotify.client.id", Objects.requireNonNull(dotenv.get("SPOTIFY_CLIENT_ID")));
        System.setProperty("spotify.client.secret", dotenv.get("SPOTIFY_CLIENT_SECRET"));
        System.setProperty("spring.security.oauth2.client.registration.google.client-id",
                Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_ID")));
        System.setProperty("spring.security.oauth2.client.registration.google.client-secret",
                Objects.requireNonNull(dotenv.get("GOOGLE_CLIENT_SECRET")));
        System.setProperty("app.oauth2.redirect-uri", Objects.requireNonNull(dotenv.get("APP_OAUTH2_REDIRECT_URI")));
        System.setProperty("spring.datasource.url", Objects.requireNonNull(dotenv.get("DB_URL")));
        System.setProperty("spring.datasource.username", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
        System.setProperty("spring.datasource.password", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));
    }
}