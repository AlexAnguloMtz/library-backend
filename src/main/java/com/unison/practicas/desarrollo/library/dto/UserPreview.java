package com.unison.practicas.desarrollo.library.dto;

public record UserPreview(
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