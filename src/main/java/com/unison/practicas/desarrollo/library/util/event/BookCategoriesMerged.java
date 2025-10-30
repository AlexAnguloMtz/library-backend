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
    public record MergedBookCategory(
            String categoryId,
            String name,
            Integer booksBeforeMerge,
            Integer booksAfterMerge
    ) {}

    private final MergedBookCategory targetCategory;
    private final List<MergedBookCategory> mergedCategories;

}