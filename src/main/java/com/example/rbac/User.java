package com.example.rbac;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public static User create(String username, String fullName, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                "Username must be 3-20 characters, only letters, digits and underscore"
            );
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

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