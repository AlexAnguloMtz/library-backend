package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record UserPersonalDataUpdateRequest(
        String firstName,
        String lastName,
        String phone,
        String gender
) {
}