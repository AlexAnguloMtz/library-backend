package com.unison.practicas.desarrollo.library.dto.user.request;

import com.unison.practicas.desarrollo.library.util.validation.ZipCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserAddressRequest(

        @NotBlank
        @Size(max = 100)
        String address,

        @NotBlank
        @Size(max = 40)
        String stateId,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 100)
        String district,

        @NotBlank
        @ZipCode
        String zipCode
) {
}