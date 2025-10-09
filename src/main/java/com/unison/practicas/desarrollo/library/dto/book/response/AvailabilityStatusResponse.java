package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record AvailabilityStatusResponse(
        String slug,
        String name
) {
}