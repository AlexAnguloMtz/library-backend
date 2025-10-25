package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record AuthorPopularityResponse(
        String gender,
        int ageMin,
        int ageMax,
        String authorId,
        String authorFirstName,
        String authorLastName,
        double value
) {
}