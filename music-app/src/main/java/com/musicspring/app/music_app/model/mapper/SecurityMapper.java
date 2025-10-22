package com.musicspring.app.music_app.model.mapper;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityMapper {

    public Set<String> toRoleNames(CredentialEntity credential) {
        if (credential == null || credential.getRoles() == null) {
            return Set.of();
        }
        return credential.getRoles().stream()
                .map(roleEntity -> roleEntity.getRole().name())
                .collect(Collectors.toSet());
    }

    public Set<String> toPermissionNames(CredentialEntity credential) {
        if (credential == null || credential.getRoles() == null) {
            return Set.of();
        }
        return credential.getRoles().stream()
                .flatMap(roleEntity -> roleEntity.getPermits().stream())
                .map(permitEntity -> permitEntity.getPermit().name())
                .collect(Collectors.toSet());
    }
}
