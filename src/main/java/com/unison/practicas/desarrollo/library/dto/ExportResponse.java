package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;
import org.springframework.http.MediaType;

@Builder
public record ExportResponse(
        MediaType mediaType,
        String fileName,
        byte[] fileBytes
) {
}