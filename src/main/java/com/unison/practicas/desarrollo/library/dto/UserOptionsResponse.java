package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record UserOptionsResponse(
        Iterable<OptionResponse> roles,
        Iterable<OptionResponse> states,
        Iterable<OptionResponse> genders
) {
}