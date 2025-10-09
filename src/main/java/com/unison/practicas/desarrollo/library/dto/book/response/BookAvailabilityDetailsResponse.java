package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BookAvailabilityDetailsResponse(
    int total,
    int available,
    int borrowed,
    List<BookCopyResponse> copies
) {
}