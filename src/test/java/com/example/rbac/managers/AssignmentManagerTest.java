package com.example.rbac.managers;

import com.example.rbac.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

public class AssignmentManagerTest {

    private AssignmentManager manager;
    private UserManager userManager;
    private RoleManager roleManager;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
        userManager = new UserManager();
        roleManager = new RoleManager();
    }

    @Test
    void addPermanentAssignmentAndCheckActive() {
        User user = User.create("alice", "Alice", "alice@example.com");
        userManager.add(user);

        Role role = new Role("Viewer", "Просмотр");
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Тест");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);

        manager.add(assignment);

        assertEquals(1, manager.count());
        assertTrue(manager.userHasRole(user, role));
    }

    @Test
    void duplicateActiveRoleThrowsException() {
        User user = User.create("bob", "Bob", "bob@example.com");
        userManager.add(user);

        Role role = new Role("User", "Обычный");
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Первое");
        PermanentAssignment a1 = new PermanentAssignment(user, role, meta);
        manager.add(a1);

        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Второе");
        PermanentAssignment a2 = new PermanentAssignment(user, role, meta2);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> manager.add(a2));
        assertTrue(ex.getMessage().contains("уже существует"));
    }

    @Test
    void getUserPermissions() {
        User user = User.create("test", "Test", "test@example.com");
        userManager.add(user);

        Role role1 = new Role("Role1", "Роль 1");
        role1.addPermission(new Permission("READ", "users", "Чтение"));
        roleManager.add(role1);

        Role role2 = new Role("Role2", "Роль 2");
        role2.addPermission(new Permission("WRITE", "reports", "Запись"));
        roleManager.add(role2);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Тест");
        manager.add(new PermanentAssignment(user, role1, meta));
        manager.add(new PermanentAssignment(user, role2, meta));

        Set<Permission> perms = manager.getUserPermissions(user);
        assertEquals(2, perms.size());
        assertTrue(perms.stream().anyMatch(p -> p.name().equals("READ") && p.resource().equals("users")));
    }

    @Test
    void revokeAssignment() {
        User user = User.create("revoke", "Revoke", "r@example.com");
        userManager.add(user);

        Role role = new Role("Temp", "Временная");
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Тест");
        String expires = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment assignment = new TemporaryAssignment(user, role, meta, expires, false);
        manager.add(assignment);

        assertTrue(assignment.isActive());

        manager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive());
    }
}
