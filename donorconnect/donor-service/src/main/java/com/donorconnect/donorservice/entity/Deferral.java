package com.donorconnect.donorservice.entity;
import com.donorconnect.donorservice.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "deferrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deferralId;

    @Column(nullable = false)
    private Long donorId;

    @Enumerated(EnumType.STRING)
    private DeferralType deferralType;

    private String reason;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeferralStatus status = DeferralStatus.ACTIVE;
}

