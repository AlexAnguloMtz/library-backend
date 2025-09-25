package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record GenderResponse(
        String id,
        String name,
        String slug
) {
}