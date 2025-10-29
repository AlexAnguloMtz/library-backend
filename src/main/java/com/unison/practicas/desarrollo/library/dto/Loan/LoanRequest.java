package com.unison.practicas.desarrollo.library.dto.Loan;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record LoanRequest(
        @NotNull Integer userId,
        @NotNull Integer bookCopyId,
        @NotNull Instant dueDate
) {}
