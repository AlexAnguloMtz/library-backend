package com.unison.practicas.desarrollo.library.service.book;

import org.springframework.web.multipart.MultipartFile;

public interface BookImageService {

    String saveBookImage(MultipartFile imageFile);

    String bookImageUrl(String bookImageKey);

}