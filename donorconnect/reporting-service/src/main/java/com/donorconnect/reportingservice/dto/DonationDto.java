package com.donorconnect.reportingservice.dto;
import lombok.Data;
import java.time.LocalDate;
@Data
public class DonationDto {
    private Long donationId;
    private LocalDate collectionDate;
}
