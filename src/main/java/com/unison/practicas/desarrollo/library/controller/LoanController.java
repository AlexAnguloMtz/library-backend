package com.unison.practicas.desarrollo.library.controller;

import com.unison.practicas.desarrollo.library.dto.Loan.AcquireBookRequest;
import com.unison.practicas.desarrollo.library.dto.Loan.LoanRequest;
import com.unison.practicas.desarrollo.library.entity.book.BookCopy;
import com.unison.practicas.desarrollo.library.entity.book.BookLoan;
import com.unison.practicas.desarrollo.library.service.Loan.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /** Crear nuevas copias de un libro (Adquisición) */
    @PostMapping("/acquisitions")
    public ResponseEntity<List<BookCopy>> acquire(@Valid @RequestBody AcquireBookRequest req) {
        List<BookCopy> created = loanService.acquire(req);
        return ResponseEntity
                .created(URI.create("/api/acquisitions"))
                .body(created);
    }

    /** Registrar un nuevo préstamo */
    @PostMapping("/loans")
    public ResponseEntity<BookLoan> loan(@Valid @RequestBody LoanRequest req,
                                         @RequestParam Integer responsibleId) {
        BookLoan created = loanService.loan(req, responsibleId);
        return ResponseEntity
                .created(URI.create("/api/loans/" + created.getId()))
                .body(created);
    }

    /** Registrar una devolución */
    @PostMapping("/loans/return/{bookCopyId}")
    public ResponseEntity<BookLoan> returnLoan(@PathVariable Integer bookCopyId) {
        BookLoan updated = loanService.returnLoan(bookCopyId);
        return ResponseEntity.ok(updated);
    }

    /** Verificar disponibilidad de una copia (opcional) */
    @GetMapping("/book-copies/{bookCopyId}/available")
    public ResponseEntity<Boolean> isCopyAvailable(@PathVariable Integer bookCopyId) {
        boolean available = loanService.isCopyAvailable(bookCopyId);
        return ResponseEntity.ok(available);
    }

    /** Ver préstamos activos de un usuario (opcional) */
    @GetMapping("/users/{userId}/active-loans")
    public ResponseEntity<List<BookLoan>> getActiveLoansByUser(@PathVariable Integer userId) {
        List<BookLoan> loans = loanService.getActiveLoansByUser(userId);
        return ResponseEntity.ok(loans);
    }
}