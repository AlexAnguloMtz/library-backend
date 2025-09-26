package com.unison.practicas.desarrollo.library.dto.user.request;

import com.unison.practicas.desarrollo.library.util.validation.Password;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ChangePasswordRequest(

        @NotBlank
        @Password
        String password,

        @NotBlank
        @Password
        String confirmedPassword

) { }