package com.unison.practicas.desarrollo.library.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CollectionHelpers {

    public static <T> T randomItem(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty");
        }
        int index = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(index);
    }


}