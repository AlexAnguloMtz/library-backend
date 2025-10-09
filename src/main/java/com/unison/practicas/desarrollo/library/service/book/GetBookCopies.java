package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.GetBookAvailabilityRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AvailabilityStatusResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookCopyResponse;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationRequest;
import com.unison.practicas.desarrollo.library.util.pagination.PaginationResponse;
import com.unison.practicas.desarrollo.library.util.pagination.SortRequest;
import com.unison.practicas.desarrollo.library.util.pagination.SortingOrder;
import org.jooq.DSLContext;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.unison.practicas.desarrollo.library.jooq.tables.BookCopy.BOOK_COPY;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookLoan.BOOK_LOAN;

@Component
public class GetBookCopies {

    private static final List<SortRequest> DEFAULT_SORTS = List.of(
            new SortRequest("id", SortingOrder.ASC)
    );

    private final DSLContext dsl;

    public GetBookCopies(DSLContext dsl) {
        this.dsl = dsl;
    }

    public PaginationResponse<BookCopyResponse> handle(String id, GetBookAvailabilityRequest request, PaginationRequest pagination) {
        Integer bookId = Integer.parseInt(id);

        // Condición para verificar si una copia está prestada
        Condition isBorrowedCondition = DSL.exists(
                dsl.selectOne()
                        .from(BOOK_LOAN)
                        .where(BOOK_LOAN.BOOK_COPY_ID.eq(BOOK_COPY.ID)
                                .and(BOOK_LOAN.RETURN_DATE.isNull()))
        );

        // Campo calculado para status_name directamente traducido
        var statusField = DSL.when(isBorrowedCondition, DSL.inline("Prestado"))
                .otherwise(DSL.inline("Disponible"))
                .as("status_name");

        // Condición base (filtrado por libro)
        Condition listCondition = BOOK_COPY.BOOK_ID.eq(bookId);

        // Filtro por texto
        if (request.search() != null && !request.search().isBlank()) {
            String pattern = "%" + request.search().trim() + "%";
            Condition idCondition = BOOK_COPY.ID.cast(String.class).likeIgnoreCase(pattern);
            Condition observationsCondition = BOOK_COPY.OBSERVATIONS.isNotNull()
                    .and(BOOK_COPY.OBSERVATIONS.likeIgnoreCase(pattern));
            listCondition = listCondition.and(idCondition.or(observationsCondition));
        }

        // Filtro por estado
        if (request.status() != null && !request.status().isBlank()) {
            switch (request.status()) {
                case "available" -> listCondition = listCondition.and(DSL.not(isBorrowedCondition));
                case "borrowed" -> listCondition = listCondition.and(isBorrowedCondition);
            }
        }

        // Count total items
        long totalItems = dsl.fetchCount(
                dsl.selectFrom(BOOK_COPY).where(listCondition)
        );

        // Ordenamiento
        var sorts = parseSorts(pagination.sort());
        var orderFields = sorts.stream()
                .map(sort -> {
                    if ("status".equals(sort.sort())) {
                        return SortingOrder.DESC.equals(sort.order()) ? statusField.desc() : statusField.asc();
                    } else {
                        return orderField(sort);
                    }
                })
                .toList();

        // Paginación
        int offset = pagination.page() * pagination.size();

        // Consulta principal
        var copies = dsl.select(
                        BOOK_COPY.ID,
                        BOOK_COPY.OBSERVATIONS,
                        statusField
                )
                .from(BOOK_COPY)
                .where(listCondition)
                .orderBy(orderFields)
                .offset(offset)
                .limit(pagination.size())
                .fetch();

        // Construcción de respuesta
        List<BookCopyResponse> copyResponses = copies.stream()
                .map(r -> {
                    String statusName = r.get("status_name", String.class);
                    AvailabilityStatusResponse status = new AvailabilityStatusResponse(
                            statusName.equals("Prestado") ? "borrowed" : "available", // slug interno
                            statusName // nombre ya traducido
                    );
                    return new BookCopyResponse(
                            r.get(BOOK_COPY.ID).toString(),
                            status,
                            r.get(BOOK_COPY.OBSERVATIONS)
                    );
                })
                .toList();

        long totalPages = (long) Math.ceil((double) totalItems / pagination.size());

        return PaginationResponse.<BookCopyResponse>builder()
                .items(copyResponses)
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
            case "id" -> BOOK_COPY.ID;
            case "observations" -> BOOK_COPY.OBSERVATIONS;
            default -> throw new IllegalArgumentException("Invalid sort: %s".formatted(sortRequest.sort()));
        };
        return SortingOrder.DESC.equals(sortRequest.order()) ? field.desc() : field.asc();
    }

}
