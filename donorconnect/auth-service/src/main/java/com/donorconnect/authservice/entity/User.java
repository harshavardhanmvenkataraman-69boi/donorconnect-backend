package com.donorconnect.authservice.entity;

import com.donorconnect.authservice.enums.UserRole;
import com.donorconnect.authservice.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "users") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(nullable = false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserRole role;
    @Column(unique = true, nullable = false) private String email;
    private String phone;
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserStatus status;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist public void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = UserStatus.ACTIVE;
    }
    @PreUpdate public void preUpdate() { updatedAt = LocalDateTime.now(); }
}
