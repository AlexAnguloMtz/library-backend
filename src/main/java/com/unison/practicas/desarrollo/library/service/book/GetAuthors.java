package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.response.AuthorResponse;
import com.unison.practicas.desarrollo.library.dto.book.request.GetAuthorsRequest;
import com.unison.practicas.desarrollo.library.dto.common.CountryResponse;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static com.unison.practicas.desarrollo.library.jooq.Tables.*;

@Component
public class GetAuthors {

    private static final List<SortRequest> DEFAULT_SORTS = List.of(
            new SortRequest("lastName", SortingOrder.ASC),
            new SortRequest("firstName", SortingOrder.ASC)
    );

    private final DSLContext dsl;
    private final DateTimeFormatter dateFormatter;

    public GetAuthors(DSLContext dsl) {
        this.dsl = dsl;
        this.dateFormatter = DateTimeFormatter.ofPattern("d/MMM/yyyy", Locale.forLanguageTag("es"));
    }

    public PaginationResponse<AuthorResponse> handle(GetAuthorsRequest request, PaginationRequest pagination) {

        var base = dsl.select(
                        AUTHOR.ID,
                        AUTHOR.FIRST_NAME,
                        AUTHOR.LAST_NAME,
                        AUTHOR.DATE_OF_BIRTH,
                        COUNTRY.ID.as("country_id"),
                        COUNTRY.NICENAME.as("country_name"),
                        DSL.count(BOOK_AUTHOR.BOOK_ID).as("book_count")
                )
                .from(AUTHOR)
                .leftJoin(COUNTRY).on(AUTHOR.COUNTRY_ID.eq(COUNTRY.ID))
                .leftJoin(BOOK_AUTHOR).on(AUTHOR.ID.eq(BOOK_AUTHOR.AUTHOR_ID));

        if (request.search() != null && !request.search().isBlank()) {
            String pattern = "%" + request.search().toLowerCase() + "%";
            base.where(
                DSL.cast(AUTHOR.ID, String.class).like(pattern)
                    .or(DSL.lower(AUTHOR.FIRST_NAME).like(pattern))
                    .or(DSL.lower(AUTHOR.LAST_NAME).like(pattern))
            );
        }

        if (!CollectionUtils.isEmpty(request.countryId())) {
            List<Integer> countryIds = request.countryId().stream().map(Integer::valueOf).toList();
            base.where(COUNTRY.ID.in(countryIds));
        }

        if (request.dateOfBirthMin() != null) {
            base.where(AUTHOR.DATE_OF_BIRTH.ge(request.dateOfBirthMin()));
        }

        if (request.dateOfBirthMax() != null) {
            base.where(AUTHOR.DATE_OF_BIRTH.le(request.dateOfBirthMax()));
        }

        base.groupBy(AUTHOR.ID, COUNTRY.ID);

        if (request.bookCountMin() != null) {
            base.having(DSL.count(BOOK_AUTHOR.BOOK_ID).ge(request.bookCountMin()));
        }

        if (request.bookCountMax() != null) {
            base.having(DSL.count(BOOK_AUTHOR.BOOK_ID).le(request.bookCountMax()));
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

        List<AuthorResponse> items = result.stream()
                .map(r -> {
                    CountryResponse country = r.get("country_id") != null
                            ? CountryResponse.builder()
                            .id(String.valueOf(r.get("country_id")))
                            .name(r.get("country_name", String.class))
                            .build()
                            : null;

                    return AuthorResponse.builder()
                            .id(String.valueOf(r.get(AUTHOR.ID)))
                            .firstName(r.get(AUTHOR.FIRST_NAME))
                            .lastName(r.get(AUTHOR.LAST_NAME))
                            .country(country)
                            .bookCount(r.get("book_count", Long.class).intValue())
                            .dateOfBirth(r.get(AUTHOR.DATE_OF_BIRTH) != null ? r.get(AUTHOR.DATE_OF_BIRTH) : null)
                            .build();
                })
                .toList();

        long totalPages = (long) Math.ceil((double) totalItems / pagination.size());

        return PaginationResponse.<AuthorResponse>builder()
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
            case "firstName" -> AUTHOR.FIRST_NAME;
            case "lastName" -> AUTHOR.LAST_NAME;
            case "country" -> COUNTRY.NICENAME;
            case "dateOfBirth" -> AUTHOR.DATE_OF_BIRTH;
            case "bookCount" -> DSL.count(BOOK_AUTHOR.BOOK_ID);
            default -> throw new IllegalArgumentException("Invalid sort: %s".formatted(sortRequest.sort()));
        };
        return SortingOrder.DESC.equals(sortRequest.order()) ? field.desc() : field.asc();
    }
}