package com.unison.practicas.desarrollo.library.dto.common;

import lombok.Builder;

@Builder
public record StateResponse(
        String id,
        String name,
        String code
) {
}
