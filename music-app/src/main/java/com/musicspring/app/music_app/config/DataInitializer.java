package com.musicspring.app.music_app.config;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.PermitEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.enums.Permit;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.PermitRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.repository.UserRepository;
import com.musicspring.app.music_app.security.service.JwtService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer {

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    private final PermitRepository permitRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PermitRepository permitRepository, RoleRepository roleRepository,
                           UserRepository userRepository,
                           CredentialRepository credentialRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.permitRepository = permitRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostConstruct
    @Transactional
    public void run() {
        createPermitIfNotExists();
        createRolesAndAssignPermissions();
        
        createAdminUserIfNotExists();
    }

    private void createPermitIfNotExists() {
        if(!permitRepository.findByPermit(Permit.READ).isPresent()){
            PermitEntity permitRead = PermitEntity
                    .builder()
                    .permit(Permit.READ)
                    .build();
            permitRepository.save(permitRead);
        }

        if(!permitRepository.findByPermit(Permit.WRITE).isPresent()){
            PermitEntity permitWrite = PermitEntity
                    .builder()
                    .permit(Permit.WRITE)
                    .build();
            permitRepository.save(permitWrite);
        }
    }

    private void createRolesAndAssignPermissions() {
        PermitEntity readPermit = permitRepository.findByPermit(Permit.READ)
                .orElseThrow(() -> new RuntimeException("READ Permit not found - Initialization failed!"));
        PermitEntity writePermit = permitRepository.findByPermit(Permit.WRITE)
                .orElseThrow(() -> new RuntimeException("WRITE Permit not found - Initialization failed!"));
        PermitEntity deletePermit = permitRepository.findByPermit(Permit.DELETE)
                .orElseThrow(() -> new RuntimeException("DELETE Permit not found - Initialization failed!"));

        RoleEntity userRole = roleRepository.findByRole(Role.ROLE_USER)
                .orElseGet(() -> {
                    RoleEntity newRole = RoleEntity.builder()
                            .role(Role.ROLE_USER)
                            .permits(new ArrayList<>(List.of(readPermit, writePermit)))
                            .build();
                    return newRole;
                });

        if (userRole.getId() != null) {
            userRole.getPermits().clear();
            userRole.getPermits().add(readPermit);
            userRole.getPermits().add(writePermit);
        }
        roleRepository.save(userRole);

        RoleEntity adminRole = roleRepository.findByRole(Role.ROLE_ADMIN)
                .orElseGet(() -> {
                    RoleEntity newRole = RoleEntity.builder()
                            .role(Role.ROLE_ADMIN)
                            .permits(new ArrayList<>(List.of(readPermit, writePermit, deletePermit)))
                            .build();
                    return newRole;
                });

        if (adminRole.getId() != null) {
            adminRole.getPermits().clear();
            adminRole.getPermits().add(readPermit);
            adminRole.getPermits().add(writePermit);
            adminRole.getPermits().add(deletePermit);
        }
        roleRepository.save(adminRole);
    }

    private void createAdminUserIfNotExists() {
        if (!credentialRepository.findByEmail("admin@tunecritic.com").isPresent() &&
                !userRepository.existsByUsername("admin")) {

            UserEntity adminUser = UserEntity.builder()
                    .username("admin")
                    .active(true)
                    .build();
            adminUser = userRepository.save(adminUser);

            RoleEntity adminRole = roleRepository.findByRole(Role.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            if (adminPassword == null || adminPassword.isEmpty()) {
                throw new IllegalStateException("ADMIN_PASSWORD environment variable is not set");
            }

            CredentialEntity credential = CredentialEntity.builder()
                    .email("admin@tunecritic.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .provider(AuthProvider.LOCAL)
                    .user(adminUser)
                    .roles(Set.of(adminRole))
                    .build();

            String refreshToken = jwtService.generateRefreshToken(credential);
            credential.setRefreshToken(refreshToken);

            credentialRepository.save(credential);
        }
    }
}