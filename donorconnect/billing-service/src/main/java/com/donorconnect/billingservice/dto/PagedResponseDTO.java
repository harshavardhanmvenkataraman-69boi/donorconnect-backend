package com.donorconnect.billingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable JSON shape for paginated responses.
 *
 * Spring's PageImpl serializes through Jackson with an "unstable" structure in
 * Spring Boot 3.2 and prints a startup warning recommending an explicit DTO.
 * Returning this wrapper avoids the warning AND gives the frontend a guaranteed
 * shape regardless of Spring/Jackson upgrades.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {

    private List<T> content;
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;

    public static <T> PagedResponseDTO<T> from(Page<T> page) {
        return PagedResponseDTO.<T>builder()
                .content(page.getContent())
                .number(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
