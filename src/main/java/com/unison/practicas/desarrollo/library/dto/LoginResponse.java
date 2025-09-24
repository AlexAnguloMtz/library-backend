package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record LoginResponse(
        String userId,
        String email,
        Set<String> roles,
        Set<String> permissions,
        String accessToken
) {
}