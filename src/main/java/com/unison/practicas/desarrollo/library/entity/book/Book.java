package com.unison.practicas.desarrollo.library.entity.book;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = {"authors"})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String isbn;
    private Integer year;
    private String image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private BookCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @OrderColumn(name = "position")
    private List<Author> authors = new ArrayList<>();

    public void setAuthors(List<Author> authors) {
        if (this.authors != null) {
            for (Author oldAuthor : this.authors) {
                oldAuthor.getBooks().remove(this);
            }
        }

        this.authors = authors != null ? authors : new ArrayList<>();

        for (Author author : this.authors) {
            author.getBooks().add(this);
        }
    }
    // Necesarios para el ReportController
    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Author> getAuthors() {
        return authors;
    }





}