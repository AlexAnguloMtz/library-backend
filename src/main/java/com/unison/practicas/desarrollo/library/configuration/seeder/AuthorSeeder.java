package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.repository.AuthorRepository;
import com.unison.practicas.desarrollo.library.util.factory.AuthorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
@Slf4j
public class AuthorSeeder {

    private final AuthorRepository authorRepository;
    private final AuthorFactory authorFactory;

    public AuthorSeeder(AuthorRepository authorRepository, AuthorFactory authorFactory) {
        this.authorRepository = authorRepository;
        this.authorFactory = authorFactory;
    }

    public void seed() {
        if (authorRepository.count() > 0) {
            log.debug("author table not empty, will skip seeding of authors");
            return;
        }
        int count = 300;

        log.debug("seeding {} authors...", count);

        authorRepository.saveAll(authorFactory.createAuthors(count));

        log.debug("seeded {} authors", count);
    }

}