package com.example.rbac.commands;

import com.example.rbac.commands.RBACSystem;
import com.example.rbac.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {
    private CommandParser parser;
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        CommandRegistry.registerAll(parser);
    }

    @Test
    void testUserCreateCommandLogic() {
       
        String input = "new_user\nIvan Ivanov\nivan@example.com\n";
        Scanner mockScanner = new Scanner(input);

        parser.parseAndExecute("user-create", mockScanner, system);

        assertTrue(system.getUserManager().exists("new_user"), 
            "Пользователь должен быть добавлен в UserManager после выполнения команды");
        
        system.getUserManager().findByUsername("new_user").ifPresent(u -> {
            assertEquals("Ivan Ivanov", u.fullName());
            assertEquals("ivan@example.com", u.email());
        });
    }

    @Test
    void testRoleDeleteCommandLogic() {
        system.initialize(); 
        assertTrue(system.getRoleManager().findByName("Admin").isPresent());

        String input = "Admin\nда\n";
        Scanner mockScanner = new Scanner(input);

        parser.parseAndExecute("role-delete", mockScanner, system);

        assertFalse(system.getRoleManager().findByName("Admin").isPresent(), 
            "Роль должна быть удалена после подтверждения");
    }
}