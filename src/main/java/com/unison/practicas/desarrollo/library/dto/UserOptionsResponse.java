package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record UserFiltersResponse(
        Iterable<OptionResponse> roles
) {
}
