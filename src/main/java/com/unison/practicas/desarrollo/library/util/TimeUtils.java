package com.unison.practicas.desarrollo.library.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class TimeUtils {

    public static Instant randomInstantBetween(Instant startInclusive, Instant endExclusive) {
        if (startInclusive == null || endExclusive == null) {
            throw new IllegalArgumentException("Start and end instants must not be null");
        }
        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("Start instant must be before end instant");
        }

        long startMillis = startInclusive.toEpochMilli();
        long endMillis = endExclusive.toEpochMilli();

        long randomMillis = ThreadLocalRandom.current().nextLong(startMillis, endMillis);
        return Instant.ofEpochMilli(randomMillis);
    }

    public static LocalDate randomLocalDateBetween(LocalDate startInclusive, LocalDate endExclusive) {
        if (startInclusive == null || endExclusive == null) {
            throw new IllegalArgumentException("Start and end dates must not be null");
        }
        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        long daysBetween = ChronoUnit.DAYS.between(startInclusive, endExclusive);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween);

        return startInclusive.plusDays(randomDays);
    }

}
