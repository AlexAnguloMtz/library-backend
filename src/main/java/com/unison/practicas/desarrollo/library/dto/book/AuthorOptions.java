package com.unison.practicas.desarrollo.library.dto.book;

import com.unison.practicas.desarrollo.library.dto.common.OptionResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record AuthorOptions(
        List<OptionResponse> countries
) {
}
