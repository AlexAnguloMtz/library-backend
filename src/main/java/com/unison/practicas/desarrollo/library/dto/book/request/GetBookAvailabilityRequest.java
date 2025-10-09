package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record GetBookAvailabilityRequest(

        @Size(max = 50)
        String search,

        @Size(max = 50)
        String status

) {
}