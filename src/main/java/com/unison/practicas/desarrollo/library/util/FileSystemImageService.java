package com.unison.practicas.desarrollo.library.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class FileSystemImageService {

    public String saveImage(MultipartFile file, Path folder) {
        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            String extension = getFileExtension(file.getOriginalFilename());
            String key = UUID.randomUUID() + "." + extension;
            Path target = folder.resolve(key);
            file.transferTo(target);
            return key;

        } catch (IOException e) {
            throw new RuntimeException("Could not save image", e);
        }
    }

    public void deleteImage(String key, Path rootFolder) {
        try {
            Path file = rootFolder.resolve(key);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete image", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1) : "";
    }

}