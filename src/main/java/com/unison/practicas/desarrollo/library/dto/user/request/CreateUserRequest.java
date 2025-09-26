package com.unison.practicas.desarrollo.library.dto.user.request;

import lombok.Builder;

@Builder
public record CreateUserRequest(
        PersonalDataRequest personalData,
        UserAddressRequest address,
        CreateAccountRequest account
) {
}