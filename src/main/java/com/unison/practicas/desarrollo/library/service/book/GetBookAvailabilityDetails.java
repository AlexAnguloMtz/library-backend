package com.unison.practicas.desarrollo.library.service.book;

import com.unison.practicas.desarrollo.library.dto.book.request.GetBookAvailabilityRequest;
import com.unison.practicas.desarrollo.library.dto.book.response.AvailabilityStatusResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookAvailabilityDetailsResponse;
import com.unison.practicas.desarrollo.library.dto.book.response.BookCopyResponse;
import org.jooq.DSLContext;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.unison.practicas.desarrollo.library.jooq.tables.BookCopy.BOOK_COPY;
import static com.unison.practicas.desarrollo.library.jooq.tables.BookLoan.BOOK_LOAN;

@Component
public class GetBookAvailabilityDetails {

    private final DSLContext dsl;

    public GetBookAvailabilityDetails(DSLContext dsl) {
        this.dsl = dsl;
    }

    public BookAvailabilityDetailsResponse handle(String id, GetBookAvailabilityRequest request) {
        Integer bookId = Integer.parseInt(id);

        // Condición para verificar si una copia está prestada (loan activo, return_date es NULL)
        Condition isBorrowedCondition = DSL.exists(
                dsl.selectOne()
                        .from(BOOK_LOAN)
                        .where(BOOK_LOAN.BOOK_COPY_ID.eq(BOOK_COPY.ID)
                                .and(BOOK_LOAN.RETURN_DATE.isNull()))
        );

        // --- Q1: Conteo global (total, prestados, disponibles) ---

        Condition globalCountCondition = BOOK_COPY.BOOK_ID.eq(bookId);

        // ✅ Orden corregido: primero cast(), luego as()
        Field<Integer> totalField = DSL.count().cast(Integer.class).as("total_count");
        Field<Integer> borrowedField = DSL.sum(
                DSL.when(isBorrowedCondition, 1).otherwise(0)
        ).cast(Integer.class).as("borrowed_count");

        var counts = dsl.select(totalField, borrowedField)
                .from(BOOK_COPY)
                .where(globalCountCondition)
                .fetchOne();

        int total = counts.get(totalField);
        int borrowed = counts.get(borrowedField);
        int available = total - borrowed; // Disponible = total - prestados

        // --- Q2: Listado de copias (búsqueda + estado) ---

        Condition listCondition = globalCountCondition;

        // Filtro por texto (id o observaciones)
        if (request.search() != null && !request.search().isBlank()) {
            String pattern = "%" + request.search().trim() + "%";

            Condition idCondition = BOOK_COPY.ID.cast(String.class).likeIgnoreCase(pattern);
            Condition observationsCondition = BOOK_COPY.OBSERVATIONS.isNotNull()
                    .and(BOOK_COPY.OBSERVATIONS.likeIgnoreCase(pattern));

            listCondition = listCondition.and(idCondition.or(observationsCondition));
        }

        // Filtro por estado (available / borrowed)
        if (request.status() != null && !request.status().isBlank()) {
            switch (request.status()) {
                case "available" -> listCondition = listCondition.and(DSL.not(isBorrowedCondition));
                case "borrowed" -> listCondition = listCondition.and(isBorrowedCondition);
            }
        }

        // Consulta principal de copias
        var copies = dsl.select(
                        BOOK_COPY.ID,
                        BOOK_COPY.OBSERVATIONS,
                        DSL.when(isBorrowedCondition, DSL.inline("borrowed"))
                                .otherwise(DSL.inline("available"))
                                .as("status_slug")
                )
                .from(BOOK_COPY)
                .where(listCondition)
                .fetch();

        // Construcción de respuesta
        List<BookCopyResponse> copyResponses = copies.stream()
                .map(r -> {
                    String slug = r.get("status_slug", String.class);
                    AvailabilityStatusResponse status = new AvailabilityStatusResponse(
                            slug,
                            slug.equals("borrowed") ? "Prestado" : "Disponible"
                    );
                    return new BookCopyResponse(
                            r.get(BOOK_COPY.ID).toString(),
                            status,
                            r.get(BOOK_COPY.OBSERVATIONS)
                    );
                })
                .collect(Collectors.toList());

        return BookAvailabilityDetailsResponse.builder()
                .total(total)
                .available(available)
                .borrowed(borrowed)
                .copies(copyResponses)
                .build();
    }
}
