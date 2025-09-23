package com.unison.practicas.desarrollo.library.util.pagination;

import java.util.List;

public record PaginationRequest(
        Integer page,
        Integer size,
        List<String> sort
) {
}