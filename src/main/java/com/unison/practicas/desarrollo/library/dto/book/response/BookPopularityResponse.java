package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record BookPopularityResponse(
        String gender,
        int ageMin,
        int ageMax,
        String bookId,
        String bookIsbn,
        String bookTitle,
        String bookImageUrl,
        double value
) {
}