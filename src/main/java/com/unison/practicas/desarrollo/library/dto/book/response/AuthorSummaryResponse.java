package com.unison.practicas.desarrollo.library.dto.book.response;

import com.unison.practicas.desarrollo.library.dto.common.CountryResponse;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AuthorSummaryResponse(
        String id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        CountryResponse country,
        Integer bookCount
) {
}