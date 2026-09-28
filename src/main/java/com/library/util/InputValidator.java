package com.library.util;

import java.util.Scanner;

/**
 * Wraps Scanner reads with validation so the menu never crashes on bad
 * input (non-numeric where a number is expected, empty required fields, etc.).
 */
public final class InputValidator {

    private InputValidator() {
    }

    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    public static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            int value = readInt(sc, prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }

    public static String readNonEmpty(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("This field can't be empty.");
        }
    }

    /** Allows blank input, returning the original/default value untouched signal (null). */
    public static String readOptional(Scanner sc, String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        return line.isEmpty() ? null : line;
    }
}
