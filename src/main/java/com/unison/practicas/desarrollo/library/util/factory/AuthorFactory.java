package com.unison.practicas.desarrollo.library.util.factory;

import com.github.javafaker.Faker;
import com.unison.practicas.desarrollo.library.entity.book.Author;
import com.unison.practicas.desarrollo.library.entity.common.Country;
import com.unison.practicas.desarrollo.library.repository.CountryRepository;
import com.unison.practicas.desarrollo.library.util.CollectionHelpers;
import com.unison.practicas.desarrollo.library.util.TimeUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Component
@Profile({"dev", "test"})
public class AuthorFactory {

    private final CountryRepository countryRepository;
    private final Faker faker;

    public AuthorFactory(CountryRepository countryRepository, Faker faker) {
        this.countryRepository = countryRepository;
        this.faker = faker;
    }

    public List<Author> createAuthors(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be higher than 0");
        }
        List<Country> countries = countryRepository.findAll();
        return IntStream.range(0, count)
                .mapToObj((i) -> createAuthor(i, CollectionHelpers.randomItem(countries)))
                .toList();
    }

    private Author createAuthor(int seed, Country country) {
        var author = new Author();
        author.setFirstName(faker.name().firstName());
        author.setLastName(faker.name().lastName());
        author.setCountry(country);
        author.setDateOfBirth(TimeUtils.randomLocalDateBetween(
                LocalDate.of(1500, 1, 1),
                LocalDate.of(2000, 12, 31)));
        return author;
    }

}