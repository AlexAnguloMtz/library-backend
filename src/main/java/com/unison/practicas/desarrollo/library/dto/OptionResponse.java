package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record OptionResponse(
        String value,
        String label
) {
}