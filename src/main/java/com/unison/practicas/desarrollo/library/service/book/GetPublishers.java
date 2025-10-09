package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.GetPublishersRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.PublisherResponse;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import com.unison.practicas.desarrollo.library.util.pagination.SortRequest;
import com.unison.practicas.desarrollo.library.util.pagination.SortingOrder;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.Tables.*;

@Component
public class GetPublishers {

    private static final List<SortRequest> DEFAULT_SORTS = List.of(
            new SortRequest("name", SortingOrder.ASC)
    );

    private final DSLContext dsl;

    public GetPublishers(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PaginationResponse<PublisherResponse> handle(GetPublishersRequest request, PaginationRequest pagination) {

        var base = dsl.select(
                        PUBLISHER.ID,
                        PUBLISHER.NAME,
                        DSL.count(BOOK.ID).as("book_count")
                )
                .from(PUBLISHER)
                .leftJoin(BOOK).on(PUBLISHER.ID.eq(BOOK.PUBLISHER_ID));

        if (StringUtils.hasText(request.search())) {
            String pattern = "%" + request.search().toLowerCase() + "%";
            base.where(
                    DSL.cast(PUBLISHER.ID, String.class).like(pattern)
                            .or(DSL.lower(PUBLISHER.NAME).like(pattern))
            );
        }

        base.groupBy(PUBLISHER.ID);

        if (request.bookCountMin() != null) {
            base.having(DSL.count(BOOK.ID).ge(request.bookCountMin()));
        }

        if (request.bookCountMax() != null) {
            base.having(DSL.count(BOOK.ID).le(request.bookCountMax()));
        }

        // Count total items
        Long totalItemsNullable = dsl.selectCount()
                .from(base.asTable("count_sub"))
                .fetchOne(0, long.class);

        long totalItems = totalItemsNullable == null ? 0 : totalItemsNullable;

        // Sorting
        List<SortRequest> sorts = parseSorts(pagination.sort());
        sorts.forEach(sort -> base.orderBy(orderField(sort)));

        // Pagination
        int offset = pagination.page() * pagination.size();
        base.offset(offset).limit(pagination.size());

        // Execute
        var result = base.fetch();

        List<PublisherResponse> items = result.stream()
                .map(r -> PublisherResponse.builder()
                        .id(String.valueOf(r.get(PUBLISHER.ID)))
                        .name(r.get(PUBLISHER.NAME))
                        .bookCount(r.get("book_count", Long.class).intValue())
                        .build())
                .toList();

        long totalPages = (long) Math.ceil((double) totalItems / pagination.size());

        return PaginationResponse.<PublisherResponse>builder()
                .items(items)
                .page(pagination.page())
                .size(pagination.size())
                .totalItems(totalItems)
                .totalPages((int) totalPages)
                .hasPrevious(pagination.page() > 0)
                .hasNext(pagination.page() < totalPages - 1)
                .build();
    }

    private List<SortRequest> parseSorts(List<String> sorts) {
        if (CollectionUtils.isEmpty(sorts)) {
            return defaultSorts();
        }
        return sorts.stream()
                .map(SortRequest::parse)
                .toList();
    }

    private List<SortRequest> defaultSorts() {
        return DEFAULT_SORTS;
    }

    private SortField<?> orderField(SortRequest sortRequest) {
        Field<?> field = switch (sortRequest.sort()) {
            case "name" -> PUBLISHER.NAME;
            case "bookCount" -> DSL.count(BOOK.ID);
            default -> throw new IllegalArgumentException("Invalid sort: %s".formatted(sortRequest.sort()));
        };
        return SortingOrder.DESC.equals(sortRequest.order()) ? field.desc() : field.asc();
    }
}
