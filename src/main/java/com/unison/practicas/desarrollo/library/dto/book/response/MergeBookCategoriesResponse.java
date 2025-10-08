package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record MergeBookCategoriesResponse(
        BookCategoryResponse targetCategory,
        int deletedCategories,
        int movedBooks
) {
}