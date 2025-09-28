package com.unison.practicas.desarrollo.library.dto.common;

import lombok.Builder;

@Builder
public record CountryResponse(
        String id,
        String name
) {
}