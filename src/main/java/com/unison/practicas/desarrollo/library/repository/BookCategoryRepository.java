package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Integer> {
    boolean existsByNameIgnoreCase(String name);

    Optional<BookCategory> findByNameIgnoreCase(String name);
}
