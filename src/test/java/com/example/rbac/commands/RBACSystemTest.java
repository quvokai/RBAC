package com.example.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    void testInitializeCreatesDefaultData() {
        system.initialize();

        assertTrue(system.getRoleManager().findByName("Admin").isPresent());
        assertTrue(system.getRoleManager().findByName("Manager").isPresent());

        assertTrue(system.getUserManager().exists("admin"));

        system.getUserManager().findByUsername("admin").ifPresent(admin -> {
            assertFalse(system.getAssignmentManager().findByUser(admin).isEmpty(), 
                "У администратора должно быть хотя бы одно назначение роли");
        });
    }

    @Test
    void testGenerateStatisticsOutput() {
        system.initialize();
        String stats = system.generateStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Пользователей: 1") || stats.contains("1"));
        assertTrue(stats.contains("Ролей: 3") || stats.contains("3"));
    }
}