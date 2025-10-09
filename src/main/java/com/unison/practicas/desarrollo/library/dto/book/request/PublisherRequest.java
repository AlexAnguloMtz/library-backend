package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PublisherRequest(

        @NotBlank
        @Size(max = 100)
        String name

) {
}
