package com.example.rbac.utils;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {


    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message + ": ");
            String input = scanner.nextLine().trim();
            if (required && input.isEmpty()) {
                System.out.println("Поле обязательно для заполнения!");
                continue;
            }
            return input;
        }
    }


    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message + " (" + min + "-" + max + "): ");
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println(" Число должно быть в диапазоне [" + min + ".." + max + "]");
            } catch (NumberFormatException e) {
                System.out.println(" Введите корректное число!");
            }
        }
    }


    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) return true;
            if (input.equals("no") || input.equals("n")) return false;
            System.out.println(" Введите yes или no!");
        }
    }


    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options.isEmpty()) throw new IllegalArgumentException("Список вариантов пуст");

        System.out.println(message);
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ") " + options.get(i));
        }

        int choice = promptInt(scanner, "Выберите номер", 1, options.size());
        return options.get(choice - 1);
    }
}