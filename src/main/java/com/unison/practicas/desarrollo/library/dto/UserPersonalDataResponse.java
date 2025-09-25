package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record UserPersonalDataResponse(
        String firstName,
        String lastName,
        String phone,
        String gender
) {
}