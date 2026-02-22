package com.example.rbac.filters;

import com.example.rbac.User;

public class UserFilters {

    public static UserFilter byUsername(String username) {
        return u -> u.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        String sub = substring.toLowerCase();
        return u -> u.username().toLowerCase().contains(sub);
    }

    public static UserFilter byEmail(String email) {
        return u -> u.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        return u -> u.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring) {
        String sub = substring.toLowerCase();
        return u -> u.fullName().toLowerCase().contains(sub);
    }
}