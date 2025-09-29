package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record GetBooksRequest(

        @Size(max = 50)
        String search,

        List<@Size(max = 40) String> categoryId,

        Boolean available,

        Integer yearMin,

        Integer yearMax

) {
}