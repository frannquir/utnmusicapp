package com.musicspring.app.music_app.config;

import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.repository.SongRepository;
import com.musicspring.app.music_app.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void run() {
//        RoleEntity role = RoleEntity.builder()
//                .role(Role.ROLE_USER)
//                .build();
//        roleRepository.save(role);
//
//        RoleEntity role2 = RoleEntity.builder()
//                .role(Role.ROLE_ADMIN)
//                .build();
//        roleRepository.save(role);
    }
}