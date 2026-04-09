package com.donorconnect.configservice.entity;
import com.donorconnect.configservice.enums.ConfigScope;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "system_config") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long configId;
    @Column(nullable = false, unique = true) private String configKey;
    @Column(columnDefinition = "TEXT") private String configValue;
    @Enumerated(EnumType.STRING) private ConfigScope scope;
    private Long updatedBy;
    private LocalDateTime updatedDate;
    @PrePersist @PreUpdate public void onSave() { updatedDate = LocalDateTime.now(); }
}
