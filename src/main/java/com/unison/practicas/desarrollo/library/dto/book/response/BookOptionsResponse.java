package com.unison.practicas.desarrollo.library.dto.book.response;

import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import lombok.Builder;

@Builder
public record BookOptionsResponse(
        Iterable<OptionResponse> categories
) {
}