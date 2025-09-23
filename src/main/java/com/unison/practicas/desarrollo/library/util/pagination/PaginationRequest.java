package com.unison.practicas.desarrollo.library.util.pagination;

import java.util.List;

public record PaginationRequest(
        Integer page,
        Integer size,
        List<SortRequest> sorts
) {

    public static PaginationRequest fromRaw(RawPaginationRequest rawPagination) {
        if (rawPagination == null) {
            return new PaginationRequest(1, 20, List.of());
        }

        Integer page = rawPagination.page() == null ? 1 : rawPagination.page();
        Integer size = rawPagination.size() == null ? 20 : rawPagination.size();

        List<SortRequest> sorts = rawPagination.sort() == null
                ? List.of()
                : rawPagination.sort().stream()
                .map(SortRequest::parse)
                .toList();

        return new PaginationRequest(page, size, sorts);
    }

}