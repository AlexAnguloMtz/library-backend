package com.unison.practicas.desarrollo.library.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

public record UserPreviewsQuery(
        String search,

        Set<String> role,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate registrationDateMin,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate registrationDateMax,

        Integer activeBookLoansMin,

        Integer activeBookLoansMax
) {
}