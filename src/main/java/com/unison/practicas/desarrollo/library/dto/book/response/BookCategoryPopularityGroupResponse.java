package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record BookCategoryPopularityGroupResponse(
        String gender,
        int ageMin,
        int ageMax,
        String category,
        double value
) {}