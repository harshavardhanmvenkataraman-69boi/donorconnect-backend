package com.donorconnect.inventoryservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;

/**
 * Stateless JWT utility for donor-service.
 * It only VALIDATES and READS tokens – it never issues them (auth-service does that).
 * The secret MUST match the one in auth-service application.properties.
 */
@Component
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /** Returns the email/username stored in the token subject. */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Reads the "roles" claim that auth-service embeds when generating the token.
     * Falls back gracefully if the claim is absent (older tokens).
     */
    @SuppressWarnings("unchecked")
    public List<SimpleGrantedAuthority> getAuthorities(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> roleList) {
            return roleList.stream()
                    .map(r -> new SimpleGrantedAuthority(r.toString()))
                    .toList();
        }

        // Fallback: derive the authority from a "role" claim (single value)
        Object roleClaim = claims.get("role");
        if (roleClaim != null) {
            return List.of(new SimpleGrantedAuthority(roleClaim.toString()));
        }
        return List.of();
    }

    /** Returns true only when the token signature is valid and it is not expired. */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}