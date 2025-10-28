package com.unison.practicas.desarrollo.library.util;

import org.springframework.util.StringUtils;

public class StringHelper {

    private StringHelper() {}

    public static String pascalCaseToSnakeCase(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        if (!isPascalCase(input)) {
            throw new IllegalArgumentException("Input is not PascalCase: " + input);
        }
        String snake = input.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return snake.toUpperCase();
    }

    private static boolean isPascalCase(String input) {
        return Character.isUpperCase(input.charAt(0))
                && input.chars().anyMatch(Character::isLowerCase)
                && !input.contains("_")
                && !input.contains("-")
                && !input.contains(" ");
    }
}
