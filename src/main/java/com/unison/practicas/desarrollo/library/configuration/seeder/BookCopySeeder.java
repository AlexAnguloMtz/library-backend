package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.entity.book.BookCopy;
import com.unison.practicas.desarrollo.library.repository.BookCopyRepository;
import com.unison.practicas.desarrollo.library.util.factory.BookCopyFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test"})
public class BookCopySeeder extends BaseSeeder<BookCopy> {

    private final BookCopyRepository bookCopyRepository;
    private final BookCopyFactory bookCopyFactory;

    public BookCopySeeder(BookCopyRepository bookCopyRepository, BookCopyFactory bookCopyFactory) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookCopyFactory = bookCopyFactory;
    }

    @Override
    long countExisting() {
        return bookCopyRepository.count();
    }

    @Override
    String resourceName() {
        return "book copies";
    }

    @Override
    List<BookCopy> makeItems(int count) {
        return bookCopyFactory.createBookCopies(count);
    }

    @Override
    void saveAll(List<BookCopy> items) {
        bookCopyRepository.saveAll(items);
    }
}
