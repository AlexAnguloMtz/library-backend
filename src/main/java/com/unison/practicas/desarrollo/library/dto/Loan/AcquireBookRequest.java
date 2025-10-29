package com.unison.practicas.desarrollo.library.dto.Loan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AcquireBookRequest(
        @NotNull Integer bookId,
        @Min(1) int copies
) {}
