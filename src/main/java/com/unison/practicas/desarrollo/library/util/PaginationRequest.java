package com.unison.practicas.desarrollo.library.util;

import java.util.List;

public record PaginationRequest(
        Integer page,
        Integer size,
        List<SortRequest> sorts
) {
}