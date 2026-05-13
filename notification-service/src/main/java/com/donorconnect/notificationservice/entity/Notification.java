package com.donorconnect.notificationservice.entity;
import com.donorconnect.notificationservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notifications") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long notificationId;
    @Column(nullable = false) private Long userId;
    @Column(columnDefinition = "TEXT", nullable = false) private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationStatus status;
    private LocalDateTime createdDate;
    @PrePersist public void prePersist() { createdDate = LocalDateTime.now(); if (status == null) status = NotificationStatus.UNREAD; }
}
