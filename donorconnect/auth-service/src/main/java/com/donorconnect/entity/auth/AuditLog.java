package com.donorconnect.entity.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    private Long userId;
    private String action; // records what happened(login)
    private String resource; // records where happened(auth)

    @Builder.Default // without this timestamp will be null
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String metadata;
    // You might store JSON here, like: {"oldRole": "USER", "newRole": "ADMIN"}
}

