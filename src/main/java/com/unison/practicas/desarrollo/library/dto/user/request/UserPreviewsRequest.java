package com.unison.practicas.desarrollo.library.dto.user.request;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record UserPreviewsRequest(
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