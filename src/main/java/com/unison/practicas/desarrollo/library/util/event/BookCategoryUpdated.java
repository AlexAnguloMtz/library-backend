package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class BookCategoryUpdated extends BookCategoryEvent {

    @Builder
    public record Fields(String name) { }

    private final String categoryId;
    private final Fields oldValues;
    private final Fields newValues;

}