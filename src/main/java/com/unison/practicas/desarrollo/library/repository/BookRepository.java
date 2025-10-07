package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.book.Book;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);
}
