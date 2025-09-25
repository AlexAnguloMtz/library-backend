package com.unison.practicas.desarrollo.library.dto;

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