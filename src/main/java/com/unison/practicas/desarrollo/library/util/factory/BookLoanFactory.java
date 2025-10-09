package com.unison.practicas.desarrollo.library.util.factory;

import com.unison.practicas.desarrollo.library.entity.book.BookCopy;
import com.unison.practicas.desarrollo.library.entity.book.BookLoan;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.BookCopyRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@Profile({"dev", "test"})
public class BookLoanFactory {

    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;

    public BookLoanFactory(
            BookCopyRepository bookCopyRepository,
            UserRepository userRepository
    ) {
        this.bookCopyRepository = bookCopyRepository;
        this.userRepository = userRepository;
    }

    public List<BookLoan> createBookLoans(int count) {
        List<User> users = userRepository.findAll();
        List<BookCopy> bookCopies = bookCopyRepository.findAll();
        Random random = new Random();

        if (count > bookCopies.size()) {
            throw new IllegalArgumentException(
                    "El total de préstamos no puede ser mayor al número de copias disponibles");
        }

        Collections.shuffle(bookCopies, random);
        List<BookCopy> selectedCopies = bookCopies.subList(0, count);

        Instant now = Instant.now().minus(Duration.ofDays(1));
        Instant dueDate = now.plus(Duration.ofDays(7));

        return selectedCopies.stream()
                .map(copy -> {
                    var loan = new BookLoan();
                    loan.setBookCopy(copy);
                    loan.setUser(CollectionHelpers.randomItem(users));
                    loan.setResponsible(CollectionHelpers.randomItem(users));
                    loan.setLoanDate(now);
                    loan.setDueDate(dueDate);
                    loan.setReturnDate(null);
                    return loan;
                })
                .toList();
    }

}