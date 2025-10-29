package com.unison.practicas.desarrollo.library.util.event;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class BookCategoriesMerged extends BookCategoryEvent {

    @Builder
    public record MergedBookCategory(String categoryId, String name) {}

    private final MergedBookCategory targetCategory;
    private final List<MergedBookCategory> mergedCategories;
    private final Integer booksMoved;

}