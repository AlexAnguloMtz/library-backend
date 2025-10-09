package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.GetBooksRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.BookAvailabilityResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookPreviewResponse;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import com.unison.practicas.desarrollo.library.util.pagination.SortRequest;
import com.unison.practicas.desarrollo.library.util.pagination.SortingOrder;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.Tables.*;

@Component
public class GetBooks {

    private final DSLContext dsl;
    private final BookImageService bookImageService;

    public GetBooks(DSLContext dsl, BookImageService bookImageService) {
        this.dsl = dsl;
        this.bookImageService = bookImageService;
    }

    public PaginationResponse<BookPreviewResponse> handle(GetBooksRequest filters, PaginationRequest pagination) {

        // Subquery principal: concatenación de autores
        var booksSubquery = dsl.select(
                        BOOK.ID,
                        BOOK.TITLE,
                        BOOK.ISBN,
                        BOOK.YEAR,
                        BOOK.IMAGE,
                        BOOK.CATEGORY_ID,
                        BOOK_CATEGORY.NAME.as("category"),
                        BOOK.PUBLISHER_ID,
                        PUBLISHER.NAME.as("publisher"),
                        DSL.coalesce(
                                DSL.stringAgg(AUTHOR.LAST_NAME.concat(",").concat(AUTHOR.FIRST_NAME), DSL.inline("-"))
                                        .orderBy(BOOK_AUTHOR.POSITION),
                                DSL.inline("")
                        ).as("authors_full_name")
                )
                .from(BOOK)
                .leftJoin(BOOK_CATEGORY).on(BOOK.CATEGORY_ID.eq(BOOK_CATEGORY.ID))
                .leftJoin(PUBLISHER).on(BOOK.PUBLISHER_ID.eq(PUBLISHER.ID))
                .leftJoin(BOOK_AUTHOR).on(BOOK.ID.eq(BOOK_AUTHOR.BOOK_ID))
                .leftJoin(AUTHOR).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHOR.ID))
                .groupBy(BOOK.ID, BOOK.TITLE, BOOK.ISBN, BOOK.YEAR, BOOK.IMAGE, BOOK.CATEGORY_ID, BOOK_CATEGORY.NAME, BOOK.PUBLISHER_ID, PUBLISHER.NAME)
                .asTable("b");

        // Condición de búsqueda
        Condition condition = DSL.trueCondition();
        if (StringUtils.hasText(filters.search())) {
            String pattern = "%" + filters.search().toLowerCase() + "%";
            condition = condition.and(
                    DSL.cast(booksSubquery.field("id", Integer.class), String.class).like(pattern)
                            .or(DSL.lower(booksSubquery.field("title", String.class)).like(pattern))
                            .or(DSL.lower(booksSubquery.field("isbn", String.class)).like(pattern))
                            .or(DSL.lower(booksSubquery.field("authors_full_name", String.class)).like(pattern))
            );
        }

        if (!CollectionUtils.isEmpty(filters.categoryId())) {
            condition = condition.and(
                    booksSubquery.field("category_id", Integer.class).in(filters.categoryId())
            );
        }

        if (!CollectionUtils.isEmpty(filters.publisherId())) {
            condition = condition.and(
                    booksSubquery.field("publisher_id", Integer.class).in(filters.publisherId())
            );
        }

        if (filters.yearMin() != null) {
            condition = condition.and(booksSubquery.field("year", Integer.class).ge(filters.yearMin()));
        }

        if (filters.yearMax() != null) {
            condition = condition.and(booksSubquery.field("year", Integer.class).le(filters.yearMax()));
        }

        // Query principal con multiset lateral para autores
        var base = dsl.select(
                        booksSubquery.field("id"),
                        booksSubquery.field("title"),
                        booksSubquery.field("isbn"),
                        booksSubquery.field("year"),
                        booksSubquery.field("image"),
                        booksSubquery.field("category"),
                        booksSubquery.field("publisher"),
                        DSL.multiset(
                                        DSL.select(AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
                                                .from(BOOK_AUTHOR)
                                                .join(AUTHOR).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHOR.ID))
                                                .where(BOOK_AUTHOR.BOOK_ID.eq(booksSubquery.field("id", Integer.class)))
                                                .orderBy(BOOK_AUTHOR.POSITION)
                                ).as("authors")
                                .convertFrom(r -> r.map(rec -> rec.get(AUTHOR.LAST_NAME) + ", " + rec.get(AUTHOR.FIRST_NAME))),
                        booksSubquery.field("authors_full_name")
                )
                .from(booksSubquery)
                .where(condition);

        // Sorting
        if (!CollectionUtils.isEmpty(pagination.sort())) {
            for (var sort : pagination.sort().stream().map(SortRequest::parse).toList()) {
                Field<?> field;
                switch (sort.sort()) {
                    case "title" -> field = booksSubquery.field("title");
                    case "isbn" -> field = booksSubquery.field("isbn");
                    case "year" -> field = booksSubquery.field("year");
                    case "author" -> field = booksSubquery.field("authors_full_name");
                    case "category" -> field = booksSubquery.field("category");
                    case "publisher" -> field = booksSubquery.field("publisher");
                    default -> field = booksSubquery.field("title");
                }
                base.orderBy(SortingOrder.DESC.equals(sort.order()) ? field.desc() : field.asc());
            }
        } else {
            base.orderBy(booksSubquery.field("title").asc());
        }

        // Paginación
        base.offset(pagination.page() * pagination.size()).limit(pagination.size());

        // Ejecutar query
        var result = base.fetch();

        // Mapear a BookResponse
        List<BookPreviewResponse> items = result.stream().map(r -> {
            List<String> authors = r.get("authors", List.class);

            var availability = BookAvailabilityResponse.builder()
                    .available(true)
                    .availableCopies(3)
                    .totalCopies(10)
                    .build();

            return BookPreviewResponse.builder()
                    .id(String.valueOf(r.get(booksSubquery.field("id", Integer.class))))
                    .title(r.get(booksSubquery.field("title", String.class)))
                    .isbn(r.get(booksSubquery.field("isbn", String.class)))
                    .year(r.get(booksSubquery.field("year", Integer.class)))
                    .imageUrl(bookImageService.bookImageUrl(r.get(booksSubquery.field("image", String.class))))
                    .authors(authors)
                    .category(r.get(booksSubquery.field("category"), String.class))
                    .publisher(r.get(booksSubquery.field("publisher"), String.class))
                    .availability(availability)
                    .build();
        }).toList();

        // Total items
        var totalItems = dsl.fetchCount(
                dsl.selectFrom(booksSubquery)
                        .where(condition)
        );

        long totalPages = (long) Math.ceil((double) totalItems / pagination.size());

        return PaginationResponse.<BookPreviewResponse>builder()
                .items(items)
                .page(pagination.page())
                .size(pagination.size())
                .totalItems(totalItems)
                .totalPages((int) totalPages)
                .hasPrevious(pagination.page() > 0)
                .hasNext(pagination.page() < totalPages - 1)
                .build();
    }
}
