package com.unison.practicas.desarrollo.library.dto.user.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record UserPreviewResponse(
     String id,
     String name,
     String email,
     String phone,
     RoleResponse role,
     String registrationDate,
     String activeLoans,
     String profilePictureUrl,
     LocalDate dateOfBirth,
     Set<String> permissions
) {
}