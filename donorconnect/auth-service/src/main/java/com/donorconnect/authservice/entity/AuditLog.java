package com.donorconnect.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "audit_logs") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long auditId;
    private Long userId;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String resource;
    @Column(nullable = false) private LocalDateTime timestamp;
    @Column(columnDefinition = "TEXT") private String metadata;
    @PrePersist public void prePersist() { timestamp = LocalDateTime.now(); }
}
