package com.unison.practicas.desarrollo.library.dto.user.request;

import com.unison.practicas.desarrollo.library.util.validation.Phone;
import com.unison.practicas.desarrollo.library.util.validation.UserDateOfBirth;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
public record PersonalDataRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Size(max = 40)
        String genderId,

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @UserDateOfBirth
        LocalDate dateOfBirth,

        @NotBlank
        @Phone
        String phone

) {
}