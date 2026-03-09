package com.example.rbac.commands;

import com.example.rbac.managers.*;
import com.example.rbac.utils.AuditLog;
import com.example.rbac.User;
import com.example.rbac.Role;
import com.example.rbac.Permission;
import com.example.rbac.AssignmentMetadata;
import com.example.rbac.PermanentAssignment;

public class RBACSystem {

    private final UserManager userManager = new UserManager();
    private final RoleManager roleManager = new RoleManager();
    private final AssignmentManager assignmentManager = new AssignmentManager();
    private final AuditLog auditLog = new AuditLog();

    private String currentUser = "system";  

    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public AuditLog getAuditLog() { return auditLog; }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {

        Permission readUsers = new Permission("READ", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Создание/редактирование пользователей");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");
        Permission readReports = new Permission("READ", "reports", "Просмотр отчётов");

        Role admin = new Role("Admin", "Полный доступ к системе");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);
        admin.addPermission(readReports);

        Role manager = new Role("Manager", "Управление пользователями");
        manager.addPermission(readUsers);
        manager.addPermission(writeUsers);

        Role viewer = new Role("Viewer", "Только просмотр");
        viewer.addPermission(readUsers);
        viewer.addPermission(readReports);

        roleManager.add(admin);
        roleManager.add(manager);
        roleManager.add(viewer);

        User adminUser = User.create("admin", "System Administrator", "admin@company.com");
        userManager.add(adminUser);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, admin, meta);
        assignmentManager.add(adminAssignment);

        auditLog.log("SYSTEM_INIT", "system", "RBACSystem", "Система инициализирована с начальными данными");
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Статистика системы ===\n");
        sb.append("Пользователей: ").append(userManager.count()).append("\n");
        sb.append("Ролей: ").append(roleManager.count()).append("\n");
        sb.append("Назначений всего: ").append(assignmentManager.count()).append("\n");
        sb.append("Активных назначений: ").append(assignmentManager.getActiveAssignments().size()).append("\n");
        sb.append("Истёкших назначений: ").append(assignmentManager.getExpiredAssignments().size()).append("\n");
        return sb.toString();
    }
}
