package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class AuthorUpdated extends AuthorEvent {

    public record Fields(
        String firstName,
        String lastName,
        String nationality,
        LocalDate dateOfBirth
    ) { }

    private final String authorId;
    private final Fields oldValues;
    private final Fields newValues;

}