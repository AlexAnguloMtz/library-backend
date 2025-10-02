package com.unison.practicas.desarrollo.library.dto.user.response;

import lombok.Builder;

@Builder
public record CreateUserResponse(
    String id,
    PersonalDataResponse personalData,
    AccountResponse account
) {
}