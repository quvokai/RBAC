package com.example.rbac.managers;

import com.example.rbac.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    private UserManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
    }

    @Test
    void addUserAndFindByUsername() {
        User user = User.create("testuser", "Test User", "test@example.com");
        manager.add(user);

        assertEquals(1, manager.count());
        assertTrue(manager.exists("testuser"));
        assertEquals("Test User", manager.findByUsername("testuser").get().fullName());
    }

    @Test
    void updateUser() {
        User user = User.create("alice", "Alice Old", "alice@old.com");
        manager.add(user);

        manager.update("alice", "Alice New", "alice@new.com");

        User updated = manager.findByUsername("alice").get();
        assertEquals("Alice New", updated.fullName());
        assertEquals("alice@new.com", updated.email());
    }

    @Test
    void addDuplicateThrowsException() {
        User u1 = User.create("dup", "Dup", "dup@example.com");
        manager.add(u1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            User u2 = User.create("dup", "Dup2", "dup2@example.com");
            manager.add(u2);
        });

        assertTrue(exception.getMessage().contains("уже существует"));
    }
}
