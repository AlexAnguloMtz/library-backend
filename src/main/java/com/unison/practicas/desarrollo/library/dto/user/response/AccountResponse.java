package com.unison.practicas.desarrollo.library.dto.user.response;

import lombok.Builder;

@Builder
public record AccountResponse(
        String email,
        RoleResponse role,
        String profilePictureUrl
) {
}