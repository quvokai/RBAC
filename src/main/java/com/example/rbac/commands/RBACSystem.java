package com.example.rbac.commands;

// Импорты менеджеров и моделей
import com.example.rbac.managers.*;
import com.example.rbac.User;
import com.example.rbac.Role;
import com.example.rbac.Permission;
import com.example.rbac.AssignmentMetadata;
import com.example.rbac.PermanentAssignment;
import com.example.rbac.TemporaryAssignment;

// Импорты утилит
import com.example.rbac.utils.AuditLog;
import com.example.rbac.utils.ReportGenerator;
import com.example.rbac.utils.BackgroundExecutor; // <-- Добавлено

public class RBACSystem {
    private final UserManager userManager = new UserManager();
    private final RoleManager roleManager = new RoleManager();
    private final AssignmentManager assignmentManager = new AssignmentManager();
    private final AuditLog auditLog = new AuditLog();
    // Инициализируем BackgroundExecutor (убедитесь, что у него есть public конструктор или используйте Singleton)
    private final BackgroundExecutor backgroundExecutor = new BackgroundExecutor();

    private String currentUser = "system";

    // Геттеры для менеджеров
    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public AuditLog getAuditLog() { return auditLog; }
    
    // --- ЭТОГО МЕТОДА НЕ ХВАТАЛО ---
    public BackgroundExecutor getBackgroundExecutor() { return backgroundExecutor; }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        // Пример инициализации базовых данных
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

        auditLog.log("SYSTEM_INIT", "system", "RBACSystem", "Система инициализирована");
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

    public void shutdown() {
        System.out.println("Завершение работы системы...");
        backgroundExecutor.shutdown();
        auditLog.shutdown();
        System.out.println("Система остановлена.");
    }
}