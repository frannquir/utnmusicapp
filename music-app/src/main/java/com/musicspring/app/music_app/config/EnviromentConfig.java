package com.musicspring.app.music_app.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class EnviromentConfig {
    static {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        try {
            Objects.requireNonNull(System.getProperty("JWT_SECRET"), "JWT_SECRET no está definida en el .env");
            Objects.requireNonNull(System.getProperty("GOOGLE_CLIENT_ID"), "GOOGLE_CLIENT_ID no está definida en el .env");
            Objects.requireNonNull(System.getProperty("GOOGLE_CLIENT_SECRET"), "GOOGLE_CLIENT_SECRET no está definida en el .env");
            Objects.requireNonNull(System.getProperty("APP_OAUTH2_REDIRECT_URI"), "APP_OAUTH2_REDIRECT_URI no está definida en el .env");
            Objects.requireNonNull(System.getProperty("DB_URL"), "DB_URL no está definida en el .env");
            Objects.requireNonNull(System.getProperty("DB_USERNAME"), "DB_USERNAME no está definido en el .env");
            Objects.requireNonNull(System.getProperty("DB_PASSWORD"), "DB_PASSWORD no está definida en el .env");
        } catch (NullPointerException e) {

            System.err.println("Configuration fatal error: " + e.getMessage());

            System.exit(1);
        }

    }
}