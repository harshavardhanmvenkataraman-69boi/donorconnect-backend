package com.donorconnect.configservice.entity;

import com.donorconnect.configservice.enums.ConfigScope;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;

    @Column(nullable = false, unique = true)
    private String configKey;

    @Column(columnDefinition = "TEXT")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConfigScope scope = ConfigScope.GLOBAL;

    private String updatedBy;

    @Builder.Default
    private LocalDateTime updatedDate = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void onSave() {
        updatedDate = LocalDateTime.now();
    }
}