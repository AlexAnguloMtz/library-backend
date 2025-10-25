package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record LoansDistributionResponse(
        int year,
        int month,
        String gender,
        int value
) {
}