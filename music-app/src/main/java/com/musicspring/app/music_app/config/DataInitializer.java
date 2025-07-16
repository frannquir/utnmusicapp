package com.musicspring.app.music_app.config;

import com.musicspring.app.music_app.security.entity.CredentialEntity;
import com.musicspring.app.music_app.security.entity.RoleEntity;
import com.musicspring.app.music_app.security.enums.AuthProvider;
import com.musicspring.app.music_app.security.enums.Role;
import com.musicspring.app.music_app.security.repository.CredentialRepository;
import com.musicspring.app.music_app.security.repository.RoleRepository;
import com.musicspring.app.music_app.model.entity.UserEntity;
import com.musicspring.app.music_app.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RoleRepository roleRepository, 
                          UserRepository userRepository,
                          CredentialRepository credentialRepository,
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void run() {
        createRolesIfNotExists();
        
        createAdminUserIfNotExists();
    }
    
    private void createRolesIfNotExists() {
        if (!roleRepository.findByRole(Role.ROLE_USER).isPresent()) {
            RoleEntity userRole = RoleEntity.builder()
                    .role(Role.ROLE_USER)
                    .build();
            roleRepository.save(userRole);
        }
        
        if (!roleRepository.findByRole(Role.ROLE_ADMIN).isPresent()) {
            RoleEntity adminRole = RoleEntity.builder()
                    .role(Role.ROLE_ADMIN)
                    .build();
            roleRepository.save(adminRole);
        }
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
            
            CredentialEntity credential = CredentialEntity.builder()
                    .email("admin@tunecritic.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .provider(AuthProvider.LOCAL)
                    .user(adminUser)
                    .roles(Set.of(adminRole))
                    .refreshToken("dummy-refresh-token-for-admin")
                    .build();
            
            credentialRepository.save(credential);
            
            System.out.println("✅ Admin user created:");
            System.out.println("   Email: admin@tunecritic.com");
            System.out.println("   Password: Admin123!");
        } else {
            System.out.println("ℹ️  Admin user already exists - skipping creation");
        }
    }
}