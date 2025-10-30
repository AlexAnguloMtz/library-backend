package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class BookCreated extends BookEvent {

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

    @Builder
    public record Author(
            String id,
            String firstName,
            String lastName
    ) {}

    private String bookId;
    private String title;
    private String isbn;
    private Integer year;
    private Category category;
    private Publisher publisher;
    private List<Author> authors;

}