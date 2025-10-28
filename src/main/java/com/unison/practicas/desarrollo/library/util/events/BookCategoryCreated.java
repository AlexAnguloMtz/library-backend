package com.unison.practicas.desarrollo.library.util.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public final class BookCategoryCreated extends BookCategoryEvent {
    private final Integer categoryId;
    private final String categoryName;
}