package com.donorconnect.security;

import com.donorconnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

// this class is the one that provided the data as this is the class that is bridge between your database and security
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // it goes to your user repo and excecutes findByEmail, if user doesn't exist throws UernameNotFoundException and if exist returs UserDetails object(which contains hashed password and role)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
// password encoder using bcrypt password encoder checks for the hashed password with the one raw password user typed