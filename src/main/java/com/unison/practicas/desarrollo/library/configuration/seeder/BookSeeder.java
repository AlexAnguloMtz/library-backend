package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.repository.BookRepository;
import com.unison.practicas.desarrollo.library.util.factory.BookFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test"})
class BookSeeder extends BaseSeeder<Book> {

    private final BookRepository bookRepository;
    private final BookFactory bookFactory;

    public BookSeeder(BookRepository bookRepository, BookFactory bookFactory) {
        this.bookRepository = bookRepository;
        this.bookFactory = bookFactory;
    }

    @Override
    long countExisting() {
        return bookRepository.count();
    }

    @Override
    String resourceName() {
        return "books";
    }

    @Override
    void saveAll(List<Book> books) {
        bookRepository.saveAll(books);
    }

    @Override
    List<Book> makeItems(int count) {
        return bookFactory.createBooks(count);
    }

}