package com.unison.practicas.desarrollo.library.dto.user.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PersonalDataResponse(
        String firstName,
        String lastName,
        String phone,
        LocalDate dateOfBirth,
        Integer age,
        GenderResponse gender
) {
}