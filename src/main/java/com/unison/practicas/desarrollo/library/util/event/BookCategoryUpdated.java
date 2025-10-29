package com.unison.practicas.desarrollo.library.util.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public final class BookCategoryUpdated extends BookCategoryEvent {

    @Builder
    public record Fields(
            String name
    ) { }

    private String categoryId;
    private Fields oldValues;
    private Fields newValues;

}