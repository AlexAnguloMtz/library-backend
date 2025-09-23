package com.unison.practicas.desarrollo.library.util.pagination;

import java.util.List;

public record RawPaginationRequest(
        Integer page,
        Integer size,
        List<String> sort
) {
}