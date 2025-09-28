package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.repository.BookRepository;
import com.unison.practicas.desarrollo.library.util.factory.BookFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
public class BookSeeder {

    private final BookRepository bookRepository;
    private final BookFactory bookFactory;

    public BookSeeder(BookRepository bookRepository, BookFactory bookFactory) {
        this.bookRepository = bookRepository;
        this.bookFactory = bookFactory;
    }

    public void seed() {
        if (bookRepository.count() > 0) {
            return;
        }
        bookRepository.saveAll(bookFactory.createBooks(600));
    }

}
