package com.unison.practicas.desarrollo.library.dto.user.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record FullUserResponse(
        String id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        RoleResponse role,
        String registrationDate,
        String profilePictureUrl,
        UserAddressResponse address,
        GenderResponse gender,
        LocalDate dateOfBirth,
        Integer age,
        Boolean canLogin,
        Set<String> permissions
) {
}