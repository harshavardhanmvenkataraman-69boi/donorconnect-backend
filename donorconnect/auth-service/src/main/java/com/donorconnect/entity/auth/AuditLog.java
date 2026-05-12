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
    private String userName;
    private String userRole;
    private String action;
    private String resource;


    // If I don't specifically set a value for this field during the build process,
    // use the default value I provided in the class
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String metadata;
}

// .build()	Finalizes the object creation.