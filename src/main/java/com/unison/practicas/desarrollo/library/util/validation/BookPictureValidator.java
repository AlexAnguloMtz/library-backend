package com.unison.practicas.desarrollo.library.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class BookPictureValidator implements ConstraintValidator<BookPicture, MultipartFile> {

    private long maxSize;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");

    @Override
    public void initialize(BookPicture constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }

        if (file.getSize() > maxSize) {
            return false;
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return false;
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

}
