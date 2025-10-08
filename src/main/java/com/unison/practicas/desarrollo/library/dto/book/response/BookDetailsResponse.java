package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BookDetailsResponse(
        String id,
        String title,
        String isbn,
        Integer year,
        BookCategoryMinimalResponse category,
        List<AuthorSummaryResponse> authors,
        String imageUrl
) {
}