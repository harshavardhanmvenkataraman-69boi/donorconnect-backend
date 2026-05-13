package com.donorconnect.bloodsupplyservice.entity;
import com.donorconnect.bloodsupplyservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "components")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;

    @Column(nullable = false)
    private Long donationId;

    @Enumerated(EnumType.STRING)
    private ComponentType componentType;

    private String bagNumber;

    private Integer volume;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    private String bloodGroup;

    private String rhFactor;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ComponentStatus status = ComponentStatus.AVAILABLE;
}