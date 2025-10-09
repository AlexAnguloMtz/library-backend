package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record BookCopyResponse(
        String id,
        AvailabilityStatusResponse status,
        String observations
) {
}