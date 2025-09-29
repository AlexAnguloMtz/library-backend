package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record BookCategoryResponse(
        String id,
        String name,
        Integer bookCount
) {
}
