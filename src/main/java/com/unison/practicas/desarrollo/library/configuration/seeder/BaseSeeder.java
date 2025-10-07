package com.unison.practicas.desarrollo.library.configuration.seeder;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
abstract class BaseSeeder<T> {

    abstract long countExisting();

    abstract int countToSeed();

    abstract String resourceName();

    abstract List<T> makeItems(int count);

    abstract void saveAll(List<T> items);

    public void seed() {
        if (countExisting() > 0) {
            log.debug("found {}, will skip seeding", resourceName());
            return;
        }

        int count = countToSeed();

        log.debug("seeding {} {}...", count, resourceName());

        saveAll(makeItems(count));

        log.debug("seeded {} {}", count, resourceName());
    }


}