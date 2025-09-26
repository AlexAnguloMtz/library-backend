package com.unison.practicas.desarrollo.library.dto.user.response;

import com.unison.practicas.desarrollo.library.dto.common.StateResponse;
import lombok.Builder;

@Builder
public record UserAddressResponse(
        String address,
        StateResponse state,
        String city,
        String district,
        String zipCode
) {
}