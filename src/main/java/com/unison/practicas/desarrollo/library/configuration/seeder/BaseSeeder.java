package com.unison.practicas.desarrollo.library.configuration.seeder;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
abstract class BaseSeeder<T> {

    abstract long countExisting();

    abstract String resourceName();

    abstract List<T> makeItems(int count);

    abstract void saveAll(List<T> items);

    public void seed(int count) {
        if (countExisting() > 0) {
            log.debug("found {}, will skip seeding", resourceName());
            return;
        }

        log.debug("seeding {} {}...", count, resourceName());

        saveAll(makeItems(count));

        log.debug("seeded {} {}", count, resourceName());
    }


}