package com.donorconnect.transfusionservice.entity;
import com.donorconnect.transfusionservice.enums.Compatibility;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "crossmatch_results") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CrossmatchResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long crossmatchId;
    @Column(nullable = false) private Long requestId;
    @Column(nullable = false) private Long componentId;
    @Enumerated(EnumType.STRING) private Compatibility compatibility;
    private Long testedBy;
    private LocalDate testedDate;
    private String status;
    private LocalDateTime createdAt;
    @PrePersist public void prePersist() { createdAt = LocalDateTime.now(); }
}
