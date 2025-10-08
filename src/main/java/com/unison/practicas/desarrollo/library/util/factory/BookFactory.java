package com.unison.practicas.desarrollo.library.util.factory;

import net.datafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.book.Book;
import com.unison.practicas.desarrollo.library.entity.book.BookCategory;
import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.repository.BookCategoryRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class BookFactory {

    private final AuthorRepository authorRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final Faker faker;
    
    public BookFactory(AuthorRepository authorRepository, BookCategoryRepository bookCategoryRepository, Faker faker) {
        this.authorRepository = authorRepository;
        this.bookCategoryRepository = bookCategoryRepository;
        this.faker = faker;
    }
    
    public List<Book> createBooks(int count) {
        List<BookCategory> categories = bookCategoryRepository.findAll();
        List<Author> authors = authorRepository.findAll();
        List<String> uniqueIsbns = new ArrayList<>(makeUniqueBookIsbns(count));
        return IntStream.range(0, count)
                .mapToObj(i -> createBook(i, CollectionHelpers.randomItem(categories), authors,  uniqueIsbns.get(i)))
                .toList();
    }

    private Book createBook(int seed, BookCategory category, List<Author> authors, String isbn) {
        var book = new Book();
        book.setTitle(faker.book().title());
        book.setIsbn(isbn);
        book.setYear(faker.number().numberBetween(1300, 2025));
        book.setCategory(category);
        book.setImage(randomBookImage());

        book.setAuthors(pickRandomAuthors(authors, faker.random().nextInt(1, 4)));

        return book;
    }

    private List<Author> pickRandomAuthors(List<Author> authors, int count) {
        if (authors.isEmpty()) return new ArrayList<>();

        List<Author> copy = new ArrayList<>(authors);

        Collections.shuffle(copy);

        int n = Math.min(count, copy.size());

        return new ArrayList<>(copy.subList(0, n));
    }

    private String randomBookImage() {
        return CollectionHelpers.randomItem(List.of(
                "book_1.jpg",
                "book_2.jpg",
                "book_3.jpg",
                "book_4.jpg",
                "book_5.jpg",
                "book_6.jpg",
                "book_7.jpg",
                "book_8.jpg",
                "book_9.jpg",
                "book_10.jpg"
        ));
    }

    private Set<String> makeUniqueBookIsbns(int count) {
        var isbns = new HashSet<String>();
        while (isbns.size() < count) {
            isbns.add(faker.code().isbn13(false));
        }
        return isbns;
    }

}