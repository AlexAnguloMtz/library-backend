package com.unison.practicas.desarrollo.library.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AccountRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 40)
        String roleId,

        @NotBlank
        @Size(min = 8, max = 100)
        String password

) {
}