package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record BookAvailabilityResponse(
        Boolean available,
        Integer availableCopies,
        Integer totalCopies
) {
}