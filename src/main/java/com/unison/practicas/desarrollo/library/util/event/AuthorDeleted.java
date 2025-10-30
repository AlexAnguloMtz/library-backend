package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class AuthorDeleted extends AuthorEvent {
    private final String authorId;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String nationality;
    private final Integer booksCount;
}