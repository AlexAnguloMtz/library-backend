package com.unison.practicas.desarrollo.library.dto.user.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record AccountResponse(
        String email,
        RoleResponse role,
        String profilePictureUrl,
        Set<String> permissions
) {
}