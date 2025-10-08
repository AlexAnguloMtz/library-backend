package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.util.factory.AuthorFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test"})
class AuthorSeeder extends BaseSeeder<Author> {

    private final AuthorRepository authorRepository;
    private final AuthorFactory authorFactory;

    public AuthorSeeder(AuthorRepository authorRepository, AuthorFactory authorFactory) {
        this.authorRepository = authorRepository;
        this.authorFactory = authorFactory;
    }

    @Override
    long countExisting() {
        return authorRepository.count();
    }

    @Override
    String resourceName() {
        return "authors";
    }

    @Override
    void saveAll(List<Author> authors) {
        authorRepository.saveAll(authors);
    }

    @Override
    List<Author> makeItems(int count) {
        return authorFactory.createAuthors(count);
    }

}