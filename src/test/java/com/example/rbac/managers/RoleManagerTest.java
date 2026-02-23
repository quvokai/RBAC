package com.example.rbac.managers;

import com.example.rbac.*;
import com.example.rbac.filters.RoleFilters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RoleManagerTest {

    private RoleManager manager;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
    }

    @Test
    void addAndFindByName() {
        Role role = new Role("Admin", "Полный доступ");
        manager.add(role);

        assertTrue(manager.exists("Admin"));
        assertEquals("Admin", manager.findByName("Admin").get().getName());
    }

    @Test
    void addDuplicateThrowsException() {
        Role r1 = new Role("User", "Обычный пользователь");
        manager.add(r1);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            Role r2 = new Role("User", "Дубликат");
            manager.add(r2);
        });

        assertTrue(ex.getMessage().contains("уже существует"));
    }

    @Test
    void addPermissionToRole() {
        Role role = new Role("Viewer", "Просмотр");
        manager.add(role);

        Permission p = new Permission("READ", "reports", "Просмотр отчётов");
        manager.addPermissionToRole("Viewer", p);

        Role updated = manager.findByName("Viewer").get();
        assertTrue(updated.hasPermission("READ", "reports"));
    }

    @Test
    void findRolesWithPermission() {
        Role r1 = new Role("Admin", "Админ");
        Role r2 = new Role("Editor", "Редактор");
        manager.add(r1);
        manager.add(r2);

        Permission p = new Permission("WRITE", "users", "Редактирование пользователей");
        manager.addPermissionToRole("Admin", p);
        manager.addPermissionToRole("Editor", p);

        List<Role> found = manager.findRolesWithPermission("WRITE", "users");
        assertEquals(2, found.size());
    }

    @Test
    void findByFilter() {
        Role r1 = new Role("SuperAdmin", "Супер");
        Role r2 = new Role("Guest", "Гость");
        manager.add(r1);
        manager.add(r2);

        List<Role> result = manager.findByFilter(RoleFilters.byNameContains("Admin"));
        assertEquals(1, result.size());
        assertEquals("SuperAdmin", result.get(0).getName());
    }
}
