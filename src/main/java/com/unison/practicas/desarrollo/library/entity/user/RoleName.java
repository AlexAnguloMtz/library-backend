package com.unison.practicas.desarrollo.library.entity.user;

import java.util.Optional;
import java.util.stream.Stream;

public enum RoleName {

    ADMIN,
    LIBRARIAN,
    USER;

    public static Optional<RoleName> parse(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim().toUpperCase();

        return Stream.of(values())
                .filter(r -> r.name().equals(normalized))
                .findFirst();
    }

}
