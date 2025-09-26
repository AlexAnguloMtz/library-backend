package com.unison.practicas.desarrollo.library.dto.user.response;

public record UserPreviewResponse(
     String id,
     String name,
     String email,
     String phone,
     RoleResponse role,
     String registrationDate,
     String activeLoans,
     String profilePictureUrl
) {
}