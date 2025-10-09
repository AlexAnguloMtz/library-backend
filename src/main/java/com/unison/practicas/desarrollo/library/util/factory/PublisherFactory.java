package com.unison.practicas.desarrollo.library.util.factory;

import com.unison.practicas.desarrollo.library.entity.book.Publisher;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@Profile({"dev", "test"})
public class PublisherFactory {

    private final Faker faker;

    public PublisherFactory(Faker faker) {
        this.faker = faker;
    }

    public List<Publisher> createPublishers(int count) {
        List<String> uniquePublishers = new ArrayList<>(makeUniquePublishers(count));
        return IntStream.range(0, count)
                .mapToObj(i -> createPublisher(i, uniquePublishers.get(i)))
                .toList();
    }

    private Set<String> makeUniquePublishers(int count) {
        return Stream.generate(() -> faker.book().publisher())
                .distinct()
                .limit(count)
                .collect(Collectors.toSet());
    }

    private Publisher createPublisher(int seed, String name) {
        var publisher = new Publisher();
        publisher.setName(name);
        return publisher;
    }

}