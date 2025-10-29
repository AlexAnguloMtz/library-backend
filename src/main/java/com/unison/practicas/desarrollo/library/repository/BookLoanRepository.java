package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.book.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookLoanRepository extends JpaRepository<BookLoan, Integer> {

    // ¿La copia está actualmente prestada?
    boolean existsByBookCopy_IdAndReturnDateIsNull(Integer bookCopyId);

    // Préstamo activo de una copia (si existe)
    Optional<BookLoan> findFirstByBookCopy_IdAndReturnDateIsNull(Integer bookCopyId);

    // Préstamos activos por usuario
    List<BookLoan> findByUser_IdAndReturnDateIsNull(Integer userId);
}