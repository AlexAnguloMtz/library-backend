package com.unison.practicas.desarrollo.library.entity.book;

import com.unison.practicas.desarrollo.library.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.Optional;

@Entity
@Data
public class BookLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_id")
    private User responsible;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_copy_id")
    private BookCopy bookCopy;

    private Instant loanDate;

    private Instant dueDate;

    private Instant returnDate;

    public Optional<Instant> getReturnDate() {
        return Optional.ofNullable(returnDate);
    }

    // Getters requeridos por el ReportController
    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public Instant getLoanDate() {
        return loanDate;
    }

    public Instant getDueDate() {
        return dueDate;
    }






}