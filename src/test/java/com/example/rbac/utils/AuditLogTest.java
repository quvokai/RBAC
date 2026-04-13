package com.example.rbac.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void testLogAndGetAll() {
        auditLog.log("ADD_USER", "admin", "user123", "Детали");
        auditLog.log("UPDATE_ROLE", "system", "role1", "Изменено");

        auditLog.flush(); // Ждем, пока фоновый поток переложит данные из очереди в список

        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals(2, entries.size());
        assertEquals("ADD_USER", entries.get(0).action());
    }

    @Test
    void testGetByPerformer() {
        auditLog.log("ACTION1", "admin", "target1", "det1");
        auditLog.log("ACTION2", "system", "target2", "det2");
        auditLog.log("ACTION3", "admin", "target3", "det3");

        auditLog.flush(); // Ждем завершения записи

        List<AuditLog.AuditEntry> byAdmin = auditLog.getByPerformer("admin");
        assertEquals(2, byAdmin.size());
    }

    @Test
    void testGetByAction() {
        auditLog.log("ADD", "admin", "target1", "det1");
        auditLog.log("UPDATE", "admin", "target2", "det2");
        auditLog.log("ADD", "system", "target3", "det3");

        auditLog.flush(); // Ждем завершения записи

        List<AuditLog.AuditEntry> adds = auditLog.getByAction("ADD");
        assertEquals(2, adds.size());
    }

    @Test
    void testPrintLog() {
        auditLog.log("TEST", "test", "target", "det");
        auditLog.printLog();  
       
    }
}
