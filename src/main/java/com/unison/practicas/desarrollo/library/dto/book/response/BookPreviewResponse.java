package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BookPreviewResponse(
        String id,
        String title,
        String isbn,
        Integer year,
        String imageUrl,
        List<String> authors,
        String category,
        String publisher,
        BookAvailabilityResponse availability
) {
}