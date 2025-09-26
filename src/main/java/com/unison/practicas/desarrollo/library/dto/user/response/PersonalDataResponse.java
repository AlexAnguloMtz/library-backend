package com.unison.practicas.desarrollo.library.dto.user.response;

import lombok.Builder;

@Builder
public record PersonalDataResponse(
        String firstName,
        String lastName,
        String phone,
        GenderResponse gender
) {
}