package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.repository.BookRepository;
import com.unison.practicas.desarrollo.library.util.factory.BookFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
@Slf4j
public class BookSeeder {

    private final BookRepository bookRepository;
    private final BookFactory bookFactory;

    public BookSeeder(BookRepository bookRepository, BookFactory bookFactory) {
        this.bookRepository = bookRepository;
        this.bookFactory = bookFactory;
    }

    public void seed() {
        if (bookRepository.count() > 0) {
            log.debug("book table not empty, will skip seeding of books");
            return;
        }

        int count = 600;

        log.debug("seeding {} books...", count);

        bookRepository.saveAll(bookFactory.createBooks(count));

        log.debug("seeded {} books", count);
    }

}