package com.unison.practicas.desarrollo.library.dto;

import java.util.List;

public record UserPreview(
     String id,
     String name,
     String email,
     String phone,
     List<RoleResponse> roles,
     String registrationDate,
     String activeLoans
) {
}