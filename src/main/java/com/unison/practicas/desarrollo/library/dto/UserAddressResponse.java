package com.unison.practicas.desarrollo.library.dto;

import lombok.Builder;

@Builder
public record UserAddressResponse(
        String address,
        String state,
        String city,
        String district,
        String zipCode
) {
}