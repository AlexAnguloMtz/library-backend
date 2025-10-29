package com.unison.practicas.desarrollo.library.util.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public final class BookCategoryCreated extends BookCategoryEvent {
    private final String categoryId;
    private final String name;
}