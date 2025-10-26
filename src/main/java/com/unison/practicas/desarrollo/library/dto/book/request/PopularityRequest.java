package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PopularityRequest(

        @Min(value = 1)
        Integer limit,

        @Size(max = 20)
        String metric

) {
}