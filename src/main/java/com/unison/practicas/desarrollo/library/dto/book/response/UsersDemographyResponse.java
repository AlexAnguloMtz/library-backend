package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record UsersDemographyResponse(
        String gender,
        int ageMin,
        int ageMax,
        int frequency
) {
}