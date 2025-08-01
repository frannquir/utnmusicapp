package com.musicspring.app.music_app.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class EnvConfig {

    @PostConstruct
    public void loadEnvironmentVariables() {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
