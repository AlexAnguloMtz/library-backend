package com.unison.practicas.desarrollo.library.dto;

import java.time.LocalDate;
import java.util.Set;

public record UserPreviewsQuery(
        String search,
        Set<String> role,
        LocalDate registrationDateMin,
        LocalDate registrationDateMax,
        Integer activeBookLoansMin,
        Integer activeBookLoansMax
) {
}