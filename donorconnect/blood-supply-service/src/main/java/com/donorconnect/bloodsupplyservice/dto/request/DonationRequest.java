package com.donorconnect.bloodsupplyservice.dto.request;

import com.donorconnect.bloodsupplyservice.enums.CollectionStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

@Data
public class DonationRequest {
    @NotNull
    private Long donorId;
    private LocalDate collectionDate;
    @NotBlank
    private String bagId;
    private Integer volumeMl;
    private String collectedBy;
    private CollectionStatus collectionStatus;
}
