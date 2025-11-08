package com.musicspring.app.music_app.security.config;

import com.musicspring.app.music_app.security.RestAuthenticationEntryPoint;
import com.musicspring.app.music_app.security.filter.JwtAuthenticationFilter;
import com.musicspring.app.music_app.security.oauth2.handlers.OAuth2AuthenticationSuccessHandler;
import com.musicspring.app.music_app.security.oauth2.services.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**","/api/v1/users/auth/**", "/login**", "/api/v1/oauth2/**","/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/login/oauth2/**","/h2-console","/h2-console/**","/api/v1/users/{id}/reactivate","/api/v1/stats/songs/mostReviewed","/api/v1/stats/albums/mostReviewed", "/api/v1/songreviews",
                                "/api/v1/albumreviews").permitAll()

                        .requestMatchers("/api/v1/users/complete-profile")
                        .hasRole("INCOMPLETE_PROFILE")

                        .requestMatchers("/api/v1/**")
                        .hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .authorizationEndpoint(authz ->
                                authz.baseUri("/api/v1/oauth2/authorization")
                        )
                        .failureUrl("http://localhost:4200/login?oauthError=true")
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e.authenticationEntryPoint(new RestAuthenticationEntryPoint()));

        return http.build();
    }
}
