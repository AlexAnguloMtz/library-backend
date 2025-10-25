package com.unison.practicas.desarrollo.library.dto.user.request;

import lombok.Builder;

@Builder
public record UpdateUserPermissionsRequest(
        Boolean login
) {
}