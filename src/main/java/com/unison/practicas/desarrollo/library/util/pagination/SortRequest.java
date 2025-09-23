package com.unison.practicas.desarrollo.library.util.pagination;

public record SortRequest(String sort, SortingOrder order) {

    public static SortRequest parse(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(
                    "Sort string cannot be null or blank. Example: 'firstName-ASC'"
            );
        }

        String[] parts = str.trim().split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Sort string must have exactly 2 parts separated by a hyphen. Example: 'firstName-ASC'. Received: '"
                            + str + "'"
            );
        }

        String field = parts[0].trim();
        if (field.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sort field cannot be empty. Received: '" + str + "'"
            );
        }

        SortingOrder order = SortingOrder.parse(parts[1])
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid sort order '" + parts[1] + "'. Allowed values: ASC, DESC"
                ));

        return new SortRequest(field, order);
    }

}