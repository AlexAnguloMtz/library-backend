package com.unison.practicas.desarrollo.library.dto.auth;

import lombok.Builder;

import java.util.Set;

@Builder
public record LoginResponse(
        String userId,
        String profilePictureUrl,
        String fullName,
        String email,
        String role,
        Set<String> permissions,
        String accessToken
) {
}