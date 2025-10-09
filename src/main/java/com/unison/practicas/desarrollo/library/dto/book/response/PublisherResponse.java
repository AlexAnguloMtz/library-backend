package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record PublisherResponse(
        String id,
        String name,
        Integer bookCount
) {
}