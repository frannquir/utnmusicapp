package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.security.dto.AuthRequest;
import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.model.dto.request.SignupRequest;
import com.musicspring.app.music_app.model.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class CredentialMapper {
    public CredentialEntity toCredentialEntity (SignupRequest signupRequest, UserEntity user) {
        return CredentialEntity.builder()
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .provider(AuthProvider.LOCAL)
                .user(user)
                .roles(null)
                .build();
    }
    public CredentialEntity toCredentialEntity (AuthRequest authRequest, UserEntity user) {
        return CredentialEntity.builder()
                .email(authRequest.emailOrUsername())
                .password(authRequest.password())
                .provider(AuthProvider.LOCAL)
                .user(user)
                .roles(null)
                .build();
    }

}
