package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record FullUser(
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
        GenderResponse gender
) {
}