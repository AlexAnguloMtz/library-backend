package com.unison.practicas.desarrollo.library.service.Loan;

import com.unison.practicas.desarrollo.library.dto.Loan.AcquireBookRequest;
import com.unison.practicas.desarrollo.library.dto.Loan.LoanRequest;
import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.entity.book.BookCopy;
import com.unison.practicas.desarrollo.library.entity.book.BookLoan;
import com.unison.practicas.desarrollo.library.entity.user.User;
import com.unison.practicas.desarrollo.library.repository.BookCopyRepository;
import com.unison.practicas.desarrollo.library.repository.BookLoanRepository;
import com.unison.practicas.desarrollo.library.repository.BookRepository;
import com.unison.practicas.desarrollo.library.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanService {

    private final BookLoanRepository loanRepo;
    private final BookCopyRepository copyRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public LoanService(BookLoanRepository loanRepo,
                       BookCopyRepository copyRepo,
                       BookRepository bookRepo,
                       UserRepository userRepo) {
        this.loanRepo = loanRepo;
        this.copyRepo = copyRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    /** Adquirir: crea N copias para un libro (tu BookCopy usa bookId, no relación @ManyToOne). */
    @Transactional
    public List<BookCopy> acquire(AcquireBookRequest req) {
        // Validamos que el libro exista
        Book book = bookRepo.findById(req.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book no encontrado: " + req.bookId()));

        List<BookCopy> toSave = new ArrayList<>();
        for (int i = 0; i < req.copies(); i++) {
            BookCopy copy = new BookCopy();
            // Tu entidad BookCopy tiene Integer bookId, así que seteamos el ID:
            copy.setBookId(book.getId());
            // Si BookCopy tiene otros campos obligatorios, setéalos aquí (e.g. estado, ubicación, etc.)
            toSave.add(copy);
        }
        return copyRepo.saveAll(toSave);
    }

    /** Prestar: valida disponibilidad y crea BookLoan. */
    @Transactional
    public BookLoan loan(LoanRequest req, Integer responsibleId) {
        // ¿La copia ya está prestada?
        if (loanRepo.existsByBookCopy_IdAndReturnDateIsNull(req.bookCopyId())) {
            throw new IllegalStateException("La copia ya está prestada");
        }

        User user = userRepo.findById(req.userId())
                .orElseThrow(() -> new IllegalArgumentException("User no encontrado: " + req.userId()));

        User responsible = userRepo.findById(responsibleId)
                .orElseThrow(() -> new IllegalArgumentException("Responsible no encontrado: " + responsibleId));

        BookCopy copy = copyRepo.findById(req.bookCopyId())
                .orElseThrow(() -> new IllegalArgumentException("BookCopy no encontrado: " + req.bookCopyId()));

        BookLoan loan = new BookLoan();
        loan.setUser(user);
        loan.setResponsible(responsible);
        loan.setBookCopy(copy);
        loan.setLoanDate(Instant.now());
        loan.setDueDate(req.dueDate()); // si quieres default, aquí puedes poner +14 días si viene null

        return loanRepo.save(loan);
    }

    /** Devolver: marca returnDate del préstamo activo de la copia. */
    @Transactional
    public BookLoan returnLoan(Integer bookCopyId) {
        BookLoan active = loanRepo.findFirstByBookCopy_IdAndReturnDateIsNull(bookCopyId)
                .orElseThrow(() -> new IllegalStateException("La copia no está prestada"));

        active.setReturnDate(Instant.now());
        return loanRepo.save(active);
    }

    /** (Útil para UI) ¿Está disponible la copia? */
    @Transactional
    public boolean isCopyAvailable(Integer bookCopyId) {
        return !loanRepo.existsByBookCopy_IdAndReturnDateIsNull(bookCopyId);
    }

    /** (Útil para UI) Préstamos activos por usuario. */
    @Transactional
    public List<BookLoan> getActiveLoansByUser(Integer userId) {
        return loanRepo.findByUser_IdAndReturnDateIsNull(userId);
    }
}