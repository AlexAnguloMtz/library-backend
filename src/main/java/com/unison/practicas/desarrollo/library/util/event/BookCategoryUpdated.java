package com.unison.practicas.desarrollo.library.util.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class BookCategoryUpdated extends BookCategoryEvent {

    @Builder
    public record Fields(String name) { }

    private String categoryId;
    private Fields oldValues;
    private Fields newValues;

}