package com.unison.practicas.desarrollo.library.entity.book;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Optional;

@Entity
@Data
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer bookId;

    private String observations;

    public Optional<String> getObservations() {
        return Optional.ofNullable(observations);
    }

    //  Método para compatibilidad con el Reporte
    public Book getBook() {
        Book b = new Book();
        try {
            java.lang.reflect.Field idField = Book.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(b, bookId);
        } catch (Exception ignored) {}
        return b;
    }






}