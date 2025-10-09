package com.unison.practicas.desarrollo.library.configuration.seeder;

import com.unison.practicas.desarrollo.library.entity.book.Publisher;
import com.unison.practicas.desarrollo.library.repository.PublisherRepository;
import com.unison.practicas.desarrollo.library.util.factory.PublisherFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test"})
public class PublisherSeeder extends BaseSeeder<Publisher> {

    private final PublisherRepository publisherRepository;
    private final PublisherFactory publisherFactory;

    public PublisherSeeder(PublisherRepository publisherRepository, PublisherFactory publisherFactory) {
        this.publisherRepository = publisherRepository;
        this.publisherFactory = publisherFactory;
    }

    @Override
    long countExisting() {
        return publisherRepository.count();
    }

    @Override
    String resourceName() {
        return "publishers";
    }

    @Override
    List<Publisher> makeItems(int count) {
        return publisherFactory.createPublishers(count);
    }

    @Override
    void saveAll(List<Publisher> items) {
        publisherRepository.saveAll(items);
    }
}
