package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class BookUpdated extends BookEvent {

    @Builder
    public record Fields(
            String title,
            String isbn,
            Integer year,
            Category category,
            Publisher publisher,
            List<Author> authors
    ) {}

    @Builder
    public record Author(
            String id,
            String firstName,
            String lastName
    ) {}

    @Builder
    public record Publisher(
            String id,
            String name
    ) {}

    @Builder
    public record Category(
            String id,
            String name
    ) {}

    private final String bookId;
    private final Fields newValues;
    private final Fields oldValues;

}