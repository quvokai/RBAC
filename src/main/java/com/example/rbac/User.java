package com.example.rbac;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public static User create(String username, String fullName, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Юзер не может быть пустым");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                "Юзер должен состоять из 3 до 20 символов, только из букв, цифр и подчеркиваний!"
            );
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Полное имя не можеи быть пустым");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Почта не может быть пустой");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Непральный формат почты");
        }
ви
        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }

    
    public static void main(String[] args) {
        try {
            User user = User.create("maxon123", "Max Dudkin", "maxdudk@example.com");
            System.out.println("Created: " + user.format());

            
            // User.create("m", "M", "m@example.com");           
            // User.create("max@", "Max", "max@example.com"); 
            // User.create("alice", "Alice", "alice@no-dot");       
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}