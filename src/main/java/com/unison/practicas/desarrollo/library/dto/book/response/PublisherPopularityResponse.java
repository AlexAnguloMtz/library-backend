package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record PublisherPopularityResponse(
        String gender,
        int ageMin,
        int ageMax,
        String publisherId,
        String publisherName,
        double value
) {
}