package com.musicspring.app.music_app.security.oauth2.dto;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OidcUser {

    private final Map<String, Object> attributes;
    @Getter
    private final CredentialEntity credential;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public CustomOAuth2User(Map<String, Object> attributes, CredentialEntity credential, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.attributes = attributes;
        this.credential = credential;
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return credential.getAuthorities();
    }

    @Override
    public String getName() {
        return credential.getEmail();
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}
