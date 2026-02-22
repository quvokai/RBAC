package com.example.rbac.filters;

import com.example.rbac.User;

@FunctionalInterface
public interface UserFilter {
    boolean test(User user);

    default UserFilter and(UserFilter other) {
        return u -> this.test(u) && other.test(u);
    }

    default UserFilter or(UserFilter other) {
        return u -> this.test(u) || other.test(u);
    }
}