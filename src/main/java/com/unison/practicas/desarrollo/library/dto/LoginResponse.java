package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record LoginResponse(
        String userId,
        String email,
        String role,
        Set<String> permissions,
        String accessToken
) {
}