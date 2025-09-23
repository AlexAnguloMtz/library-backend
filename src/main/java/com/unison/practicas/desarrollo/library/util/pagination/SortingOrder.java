package com.unison.practicas.desarrollo.library.util.pagination;

import java.util.Arrays;
import java.util.Optional;

public enum SortingOrder {
    ASC,
    DESC;

    public static Optional<SortingOrder> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(SortingOrder.values())
                .filter(o -> o.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }

}