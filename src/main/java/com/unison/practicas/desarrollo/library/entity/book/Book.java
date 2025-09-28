package com.unison.practicas.desarrollo.library.entity.book;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private BookCategory category;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    public void setAuthors(Set<Author> authors) {
        if (this.authors != null) {
            for (Author oldAuthor : this.authors) {
                oldAuthor.getBooks().remove(this);
            }
        }

        this.authors = authors != null ? authors : new HashSet<>();

        for (Author author : this.authors) {
            author.getBooks().add(this);
        }
    }

}