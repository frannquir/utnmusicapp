package com.musicspring.app.music_app.config;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.repository.SongRepository;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/* this class implements a spring boot functional interface, and string... args is part of it.
initializes data, used for testing w/ h2.
 */
@Component

public class DataInitializer {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final RoleRepository roleRepository;
    private final SongRepository songRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, 
                          CredentialRepository credentialRepository,
                          RoleRepository roleRepository,
                          SongRepository songRepository, 
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.roleRepository = roleRepository;
        this.songRepository = songRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void run() {
        RoleEntity role = RoleEntity.builder()
                .role(Role.ROLE_USER)
                .build();
        roleRepository.save(role);
    }
}