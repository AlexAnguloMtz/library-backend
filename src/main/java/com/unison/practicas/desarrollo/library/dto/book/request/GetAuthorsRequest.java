package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GetAuthorsRequest(

        @Size(max = 50)
        String search,

        @Size(max = 20)
        List<@Size(max = 40) String> countryId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateOfBirthMin,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateOfBirthMax,

        Integer bookCountMin,

        Integer bookCountMax

) {}