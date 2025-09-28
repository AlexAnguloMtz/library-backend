package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
}
