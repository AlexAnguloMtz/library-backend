package com.unison.practicas.desarrollo.library.dto.book.request;

import com.unison.practicas.desarrollo.library.util.validation.BookPicture;
import com.unison.practicas.desarrollo.library.util.validation.BookYear;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record CreateBookRequest(

        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        @ISBN
        String isbn,

        @NotNull
        @BookYear
        Integer year,

        @NotNull
        @Size(min = 1, max = 10)
        List<@Size(max = 40) String> authorIds,

        @NotBlank
        @Size(max = 40)
        String categoryId,

        @NotNull
        @BookPicture
        MultipartFile bookPicture

) {
}