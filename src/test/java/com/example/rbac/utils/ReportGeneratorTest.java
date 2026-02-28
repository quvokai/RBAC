package com.example.rbac.utils;

import com.example.rbac.managers.UserManager;
import com.example.rbac.managers.AssignmentManager;
import com.example.rbac.managers.RoleManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorTest {

    @Test
    void testGenerateUserReport() {
        UserManager userMgr = new UserManager();
        AssignmentManager assignMgr = new AssignmentManager();

        String report = ReportGenerator.generateUserReport(userMgr, assignMgr);
        assertNotNull(report);
        assertTrue(report.contains("Отчёт по пользователям"));
    }

}
