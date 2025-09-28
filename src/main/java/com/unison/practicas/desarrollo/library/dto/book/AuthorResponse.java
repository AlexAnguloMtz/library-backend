package com.unison.practicas.desarrollo.library.dto.book;

import com.unison.practicas.desarrollo.library.dto.common.CountryResponse;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AuthorResponse(
        String id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        CountryResponse country,
        int bookCount
) {
}