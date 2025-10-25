package com.unison.practicas.desarrollo.library.dto.book.response;

import lombok.Builder;

@Builder
public record UsersAcquisitionResponse(
        int year,
        String month,
        int usersAtBeginning,
        int usersAtEnd,
        int newUsers
) {
}