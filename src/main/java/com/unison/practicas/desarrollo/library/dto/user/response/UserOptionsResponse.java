package com.unison.practicas.desarrollo.library.dto.user.response;

import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import lombok.Builder;

@Builder
public record UserOptionsResponse(
        Iterable<OptionResponse> roles,
        Iterable<OptionResponse> states,
        Iterable<OptionResponse> genders
) {
}