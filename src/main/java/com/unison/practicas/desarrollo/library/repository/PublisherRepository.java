package com.unison.practicas.desarrollo.library.repository;

import com.unison.practicas.desarrollo.library.entity.book.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Publisher> findByNameIgnoreCase(String name);

}