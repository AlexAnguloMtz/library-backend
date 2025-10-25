package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record BookCategoriesPopularityRequest(

        @Min(value = 1)
        Integer limit

) {
}