package com.donorconnect.security;

import com.donorconnect.entity.auth.User;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}") // value will be extracted from .properties
    private String jwtSecret;

    @Value("${app.jwt.expiration}") // value will be extracted from .properties
    private long jwtExpiration;

    // converts your secret string into a secure cryptographic key object which is suitable for HMAC Algo
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // this method is called after successful login. it takes authentication object(all users details) and prepares token metadata
    public String generateToken(Authentication authentication) {
        String username = authentication.getName(); // get users email
        Date now = new Date(); // marks the exact time token is created
        Date expiry = new Date(now.getTime() + jwtExpiration); // calculates when the token should die

        List<String> roles = authentication.getAuthorities().stream() // extracts user roles and put them into list of strings
                .map(GrantedAuthority::getAuthority)
                .toList();

        String primaryRole = roles.isEmpty() ? "" : roles.get(0);

        String userId = null;
        if (authentication.getPrincipal() instanceof User user) {
            userId = String.valueOf(user.getUserId());
        }
        // it checks logged in user is an instance of User entity and grabs their database userId

        return Jwts.builder()
                .setSubject(username) // set email(owner of token)
                // like for eg if a user has two roles so roles would be the list of both the roles and primaryRole will be the main role
                .claim("roles", roles)   // puts the role inside token so gateway can read them without asking database
                .claim("role", primaryRole)     
                .claim("userId", userId)     // ads database id inside token
                .setIssuedAt(now) // exact time
                .setExpiration(expiry) // expiration time
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                // It signs the data using your secret key. If a hacker changes a single letter in the token, the signature will no longer match, and the token will be rejected
                .compact();
        // Squashes all this data into the final, long "base64" string that you see in Postman
    }

    // this is the method that opens the token to see who this token belongs to
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // uses the secret to unlock the token
                .build()
                .parseClaimsJws(token)// reads the data
                .getBody() // gets the payload (the claims)
                .getSubject(); // returns email
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true; // if signature is valid and not expired
        } catch (JwtException | IllegalArgumentException e) {
            return false; // if token is fake, expired
        }
    }
}