package com.donorconnect.notificationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Returns the JWT subject (username/email) as set by auth-service.
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Returns the userId stored as a dedicated claim ("userId") by auth-service.
     * The JWT subject is the username string; the numeric user ID is a separate claim.
     */
    public Long getUserIdFromToken(String token) {
        Object userIdClaim = getClaims(token).get("userId");
        if (userIdClaim == null) return null;
        try {
            return Long.valueOf(userIdClaim.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<SimpleGrantedAuthority> getAuthorities(String token) {
        Claims claims = getClaims(token);

        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> roleList) {
            return roleList.stream()
                    .map(r -> new SimpleGrantedAuthority(r.toString()))
                    .toList();
        }

        Object roleClaim = claims.get("role");
        if (roleClaim != null) {
            return List.of(new SimpleGrantedAuthority(roleClaim.toString()));
        }
        return List.of();
    }

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
