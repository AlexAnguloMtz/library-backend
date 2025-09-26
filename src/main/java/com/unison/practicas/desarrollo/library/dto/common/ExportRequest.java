package com.unison.practicas.desarrollo.library.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record ExportRequest(
        @NotBlank
        String format,

        @NotNull
        @NotEmpty
        @Size(max = 200)
        List<@NotBlank @Size(max = 40) String> ids
) {
}