package com.unison.practicas.desarrollo.library.util.pagination;

import jakarta.validation.constraints.Size;

import java.util.List;

public record PaginationRequest(

        Integer page,

        Integer size,

        @Size(max = 10)
        List<@Size(max = 30) String>  sort

) {
}