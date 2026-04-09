package com.donorconnect.bloodsupplyservice.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "inventory_balances") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryBalance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long balanceId;
    @Column(nullable = false) private Long componentId;
    private String bloodGroup;
    private String rhFactor;
    private Long locationId;
    private Integer quantity;
    private String status;
    private LocalDateTime updatedAt;
    @PrePersist @PreUpdate public void onSave() { updatedAt = LocalDateTime.now(); }
}
