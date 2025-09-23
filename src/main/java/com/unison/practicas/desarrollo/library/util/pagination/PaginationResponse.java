package com.unison.practicas.desarrollo.library.util.pagination;

import lombok.Builder;
import java.util.List;

@Builder
public record PaginationResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {}