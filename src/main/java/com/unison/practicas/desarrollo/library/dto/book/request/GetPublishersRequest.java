package com.unison.practicas.desarrollo.library.dto.book.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record GetPublishersRequest(

        @Size(max = 30)
        String search,

        Integer bookCountMin,

        Integer bookCountMax

) {}