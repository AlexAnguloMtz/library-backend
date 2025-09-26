package com.unison.practicas.desarrollo.library.dto.common;

import lombok.Builder;

@Builder
public record OptionResponse(
        String value,
        String label
) {
}