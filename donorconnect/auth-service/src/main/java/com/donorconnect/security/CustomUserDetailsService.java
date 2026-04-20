package com.donorconnect.security;

import com.donorconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service // This allows Spring to automatically find it and "inject" it into your SecurityConfig class
@RequiredArgsConstructor

public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    // loadUserByUsername -> it hits mysql database via User repo
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
