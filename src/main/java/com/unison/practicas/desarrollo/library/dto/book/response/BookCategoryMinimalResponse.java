package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record BookCategoryMinimalResponse(
        String id,
        String name
) {
}