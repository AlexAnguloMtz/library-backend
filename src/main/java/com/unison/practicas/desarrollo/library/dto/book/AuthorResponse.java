package com.unison.practicas.desarrollo.library.dto.book;

import com.unison.practicas.desarrollo.library.dto.common.CountryResponse;
import lombok.Builder;

@Builder
public record AuthorResponse(
        String id,
        String firstName,
        String lastName,
        String dateOfBirth,
        CountryResponse country,
        int bookCount
) {
}