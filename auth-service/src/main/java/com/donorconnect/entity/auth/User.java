package com.donorconnect.entity.auth;

import com.donorconnect.enums.Enums.UserRole;
import com.donorconnect.enums.Enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    // Use columnDefinition to force the database to use VARCHAR(50)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private UserRole role;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 15) // Standardizing phone length
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private UserStatus status = UserStatus.ACTIVE;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Returns the Enum name (e.g., "ROLE_INVENTORY_CONTROLLER")
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}