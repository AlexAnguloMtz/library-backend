package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record UserAccountUpdateRequest(
        String email,
        String role,
        String password
) {
}