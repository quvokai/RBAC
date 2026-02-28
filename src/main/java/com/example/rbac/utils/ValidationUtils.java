package com.example.rbac.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class ValidationUtils {

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

  
    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) return false;

        if (tryParse(date, "uuuu-MM-dd", false)) return true;
        
       
        return tryParse(date, "uuuu-MM-dd HH:mm", true);
    }

    private static boolean tryParse(String value, String pattern, boolean hasTime) {
        try {
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                    .withResolverStyle(ResolverStyle.STRICT);
            
            if (hasTime) {
                LocalDateTime.parse(value, formatter);
            } else {
                LocalDate.parse(value, formatter);
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static String normalizeString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }
}