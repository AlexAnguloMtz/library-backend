package com.unison.practicas.desarrollo.library.entity.book;

import com.unison.practicas.desarrollo.library.entity.common.Country;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(exclude = {"books"})
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    public String getReversedFullName() {
        return lastName + ", " + firstName;
    }
    // Para mostrar correctamente el nombre completo del autor
    public String getFullName() {
        return firstName + " " + lastName;
    }

}