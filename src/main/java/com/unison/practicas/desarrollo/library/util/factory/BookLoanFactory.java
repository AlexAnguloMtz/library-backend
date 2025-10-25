package com.unison.practicas.desarrollo.library.util.factory;

import com.unison.practicas.desarrollo.library.entity.book.BookCopy;
import com.unison.practicas.desarrollo.library.entity.book.BookLoan;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.BookCopyRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@Profile({"dev", "test"})
public class BookLoanFactory {

    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public BookLoanFactory(
            BookCopyRepository bookCopyRepository,
            UserRepository userRepository
    ) {
        this.bookCopyRepository = bookCopyRepository;
        this.userRepository = userRepository;
    }

    public List<BookLoan> createBookLoans(int maxTotalLoans) {
        List<BookLoan> allLoans = new ArrayList<>();
        List<User> users = userRepository.findAll();
        List<BookCopy> bookCopies = bookCopyRepository.findAll();

        if (users.isEmpty() || bookCopies.isEmpty() || maxTotalLoans <= 0) {
            return allLoans; // nada que hacer
        }

        // Barajamos para aleatoriedad total
        Collections.shuffle(bookCopies, random);

        Instant today = Instant.now();
        Instant todayMinus20 = today.minus(20, ChronoUnit.DAYS);
        Instant startDate = LocalDate.of(2015, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);

        int remainingLoans = maxTotalLoans;

        for (BookCopy copy : bookCopies) {
            if (remainingLoans <= 0) break;

            // --- Prestamos en el pasado ---
            int pastLoansCount = Math.min(random.nextInt(21), remainingLoans); // 0..20 o lo que quede
            remainingLoans -= pastLoansCount;

            Instant lastLoanEnd = startDate;
            for (int i = 0; i < pastLoansCount; i++) {
                int durationDays = 5 + random.nextInt(6); // 5..10 dias
                long remainingDays = ChronoUnit.DAYS.between(lastLoanEnd, todayMinus20);
                if (remainingDays < durationDays) break;

                long maxStartOffset = remainingDays - durationDays;
                long offsetDays = (maxStartOffset > 0) ? random.nextInt((int) maxStartOffset + 1) : 0;

                Instant loanStart = lastLoanEnd.plus(offsetDays, ChronoUnit.DAYS);
                Instant loanEnd = loanStart.plus(durationDays, ChronoUnit.DAYS);

                BookLoan loan = new BookLoan();
                loan.setBookCopy(copy);
                loan.setUser(CollectionHelpers.randomItem(users));
                loan.setResponsible(CollectionHelpers.randomItem(users));
                loan.setLoanDate(loanStart);
                loan.setDueDate(loanEnd);
                loan.setReturnDate(loanEnd); // devuelto
                allLoans.add(loan);

                lastLoanEnd = loanEnd;
            }

            // --- Prestamo actual ---
            if (remainingLoans > 0 && random.nextBoolean()) { // 50% probabilidad
                int pastOffsetDays = 1 + random.nextInt(5); // 1..5 dias atras
                Instant loanStart = today.minus(pastOffsetDays, ChronoUnit.DAYS);
                Instant loanEnd = loanStart.plus(10, ChronoUnit.DAYS);

                BookLoan loan = new BookLoan();
                loan.setBookCopy(copy);
                loan.setUser(CollectionHelpers.randomItem(users));
                loan.setResponsible(CollectionHelpers.randomItem(users));
                loan.setLoanDate(loanStart);
                loan.setDueDate(loanEnd);
                loan.setReturnDate(null); // préstamo activo
                allLoans.add(loan);

                remainingLoans--;
            }
        }

        return allLoans;
    }
}
