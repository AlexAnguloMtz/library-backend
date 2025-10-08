package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record MergeBookCategoriesRequest(

        @NotBlank
        String targetCategoryId,

        @NotNull
        @NotEmpty
        @Size(max = 10)
        Set<@NotBlank @Size(max = 40) String> mergedCategoriesIds

) {
}