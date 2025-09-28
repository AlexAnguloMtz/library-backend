package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.util.factory.AuthorFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
public class AuthorSeeder {

    private final AuthorRepository authorRepository;
    private final AuthorFactory authorFactory;

    public AuthorSeeder(AuthorRepository authorRepository, AuthorFactory authorFactory) {
        this.authorRepository = authorRepository;
        this.authorFactory = authorFactory;
    }

    public void seed() {
        if (authorRepository.count() > 0) {
            return;
        }
        authorRepository.saveAll(authorFactory.createAuthors(300));
    }

}