package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record FullUser(
        String id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        List<RoleResponse> roles,
        String registrationDate,
        String profilePictureUrl,
        UserAddressResponse address,
        GenderResponse gender
) {
}