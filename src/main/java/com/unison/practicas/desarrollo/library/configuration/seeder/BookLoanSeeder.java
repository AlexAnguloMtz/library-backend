package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.entity.book.BookLoan;
import com.unison.practicas.desarrollo.library.repository.BookLoanRepository;
import com.unison.practicas.desarrollo.library.util.factory.BookLoanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test"})
public class BookLoanSeeder extends BaseSeeder<BookLoan> {

    private final BookLoanRepository bookLoanRepository;
    private final BookLoanFactory bookLoanFactory;

    public BookLoanSeeder(BookLoanRepository bookLoanRepository, BookLoanFactory bookLoanFactory) {
        this.bookLoanRepository = bookLoanRepository;
        this.bookLoanFactory = bookLoanFactory;
    }

    @Override
    long countExisting() {
        return bookLoanRepository.count();
    }

    @Override
    String resourceName() {
        return "book loans";
    }

    @Override
    List<BookLoan> makeItems(int count) {
        return bookLoanFactory.createBookLoans(count);
    }

    @Override
    void saveAll(List<BookLoan> items) {
        bookLoanRepository.saveAll(items);
    }
}
