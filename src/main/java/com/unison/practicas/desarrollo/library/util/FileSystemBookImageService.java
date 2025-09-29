package com.unison.practicas.desarrollo.library.util;

import com.unison.practicas.desarrollo.library.service.book.BookImageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileSystemBookImageService implements BookImageService {

    private final FileSystemImageService imageService;
    private final Path rootFolder;

    public FileSystemBookImageService(FileSystemImageService imageService) {
        this.imageService = imageService;
        this.rootFolder = Paths.get("data/books/images").toAbsolutePath();
    }

    @Override
    public String saveBookImage(MultipartFile imageFile) {
        return imageService.saveImage(imageFile, rootFolder);
    }

    @Override
    public String bookImageUrl(String key) {
        String publicUrlPrefix = "http://localhost:8080/api/v1/books/images/";
        return publicUrlPrefix + key;
    }

}