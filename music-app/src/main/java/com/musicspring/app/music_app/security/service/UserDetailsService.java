package com.musicspring.app.music_app.security.service;

import com.musicspring.app.music_app.security.repository.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final CredentialRepository credentialRepository;

    @Autowired
    public UserDetailsService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return credentialRepository.findByEmailOrUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}
