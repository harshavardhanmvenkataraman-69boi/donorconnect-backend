package com.donorconnect.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration // creates bean manually using @Bean
@EnableWebSecurity // enables spring security configuration and activates web security features and also we can also change the default settings to custom settings
@EnableMethodSecurity // we can implement RBAC and also provides methods like @PreAuthorize and @PostAuthorize
@RequiredArgsConstructor // makes constructor of fields marked as final and @NonNull
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    private static final String[] PUBLIC_URLS = { // list of those endpoints which do not requires any jwt
            "/api/v1/auth/login",
            "/api/v1/auth/setup-admin",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/test/**",
            "/api-docs/**",
            "/v1/api-docs/**",
            // Frontend static resources
            "/",
            "/index.html",
            "/assets/**",
            "/favicon.svg",
            "/icons.svg",
            "/login",
            "/setup",
            "/forgot-password",
            "/reset-password"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // one way hashing -> converts raw password into hashed password and use salting(which means a random string of characters will be added in front of your hashed password so no two users can have same hashed password)

    // user details service is the one who can go in the database as it makes the bridge b/w the database and spring security and verifies the hashed password and email
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder()); // used to check if the typed password matches the hashed password
        return provider; // if both matches the password and email it will create authentication object and says it is a valid user
    }

    // it will first see the list of providers used and found only dao, so it will tell to that provider take name and email and password and verify that
    // because that provider will be the one who will verify
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(authenticationProvider()));
    }

    // before hitting controller the request must pass through this filter chain
    // csrf -> cross site request forgery
    // we disabled it because we are using stateless API (JWT)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // don't create session on server don't save user in memory
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll() // if there is any endpoint in that list let it pass without any jwt and authorization but if not present authorize that request
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // a default filter that checks for username and password it says before you look for password let my jwt filter check for a valid token and if that token is valid the rest of the security check got skipped

        return http.build();
    }
}


//@Bean
//public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http
//            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//            .csrf(csrf -> csrf.disable())
//            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authorizeHttpRequests(auth -> auth
//                    .requestMatchers(PUBLIC_URLS).permitAll()
//                    .anyRequest().authenticated()
//            )
//            .authenticationProvider(authenticationProvider())
//            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//    return http.build();
//}