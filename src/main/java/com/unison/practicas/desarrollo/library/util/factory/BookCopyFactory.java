package com.unison.practicas.desarrollo.library.util.factory;

import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.entity.book.BookCopy;
import com.unison.practicas.desarrollo.library.repository.BookRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class BookCopyFactory {

    private final BookRepository bookRepository;

    public BookCopyFactory(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookCopy> createBookCopies(int count) {
        List<Book> books = bookRepository.findAll();
        List<String> observations = sampleObservations();
        Random random = new Random();

        if (books.isEmpty() || count <= 0) {
            return List.of();
        }

        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Book book = CollectionHelpers.randomItem(books);

                    var copy = new BookCopy();
                    copy.setBookId(book.getId());

                    if (random.nextBoolean()) {
                        copy.setObservations(CollectionHelpers.randomItem(observations));
                    } else {
                        copy.setObservations(null);
                    }

                    return copy;
                })
                .toList();
    }

    private List<String> sampleObservations() {
        return List.of(
                "Libro en buen estado",
                "Cubierta ligeramente doblada",
                "Hojas limpias y completas",
                "Faltan algunas páginas",
                "Escrito a lápiz en margen",
                "Marcas de uso leves",
                "Encuadernación firme",
                "Páginas amarillentas",
                "Ligeras manchas en portada",
                "Sin daños visibles",
                "Rasguños en contraportada",
                "Cubierta un poco gastada",
                "Libro antiguo, cuidado",
                "Se recomienda limpieza",
                "Etiqueta de biblioteca",
                "Sello de préstamo previo",
                "Páginas subrayadas",
                "Libro sin cubierta",
                "Pliegues en esquinas",
                "Buen estado general"
        );
    }

}