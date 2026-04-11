package com.example.rbac.commands;

import com.example.rbac.managers.*;
import com.example.rbac.utils.AuditLog;
import com.example.rbac.utils.BackgroundExecutor;
import com.example.rbac.*;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager = new UserManager();
    private final RoleManager roleManager = new RoleManager();
    private final AssignmentManager assignmentManager = new AssignmentManager();
    private final AuditLog auditLog = new AuditLog();
    private final BackgroundExecutor backgroundExecutor = new BackgroundExecutor();
    
    // ScheduledExecutorService для периодических задач (feature/schedule-tasks)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean schedulerRunning = false;
    private static final long SCHEDULER_DELAY_SECONDS = 10;
    private static final long SCHEDULER_PERIOD_SECONDS = 30;

    private String currentUser = "system";

    // ===== Геттеры =====
    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public AuditLog getAuditLog() { return auditLog; }
    public BackgroundExecutor getBackgroundExecutor() { return backgroundExecutor; }

    public void setCurrentUser(String username) { this.currentUser = username; }
    public String getCurrentUser() { return currentUser; }

    // ===== Инициализация =====
    public void initialize() {
        // Базовые права
        Permission readUsers = new Permission("READ", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Создание/редактирование пользователей");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");
        Permission readReports = new Permission("READ", "reports", "Просмотр отчётов");

        // Роли
        Role admin = new Role("Admin", "Полный доступ к системе");
        admin.addPermission(readUsers); admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers); admin.addPermission(readReports);

        Role manager = new Role("Manager", "Управление пользователями");
        manager.addPermission(readUsers); manager.addPermission(writeUsers);

        Role viewer = new Role("Viewer", "Только просмотр");
        viewer.addPermission(readUsers); viewer.addPermission(readReports);

        roleManager.add(admin); roleManager.add(manager); roleManager.add(viewer);

        // Пользователь admin
        User adminUser = User.create("admin", "System Administrator", "admin@company.com");
        userManager.add(adminUser);

        // Назначение роли
        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, admin, meta);
        assignmentManager.add(adminAssignment);

        auditLog.log("SYSTEM_INIT", "system", "RBACSystem", "Система инициализирована");

        // Запуск периодической задачи
        startScheduledTasks();
    }

    // ===== Периодические задачи (feature/schedule-tasks) =====
    private void startScheduledTasks() {
        if (schedulerRunning) return;
        schedulerRunning = true;

        scheduler.scheduleAtFixedRate(() -> {
            try {
                processExpiredAssignments();
                logPeriodicStatistics();
            } catch (Exception e) {
                auditLog.log("SCHEDULER_ERROR", "scheduler", "RBACSystem", 
                    "Ошибка: " + e.getMessage());
                System.err.println("[Scheduler] Ошибка: " + e.getMessage());
            }
        }, SCHEDULER_DELAY_SECONDS, SCHEDULER_PERIOD_SECONDS, TimeUnit.SECONDS);

        auditLog.log("SCHEDULER_STARTED", "system", "RBACSystem", 
            "Периодическая задача запущена (интервал: " + SCHEDULER_PERIOD_SECONDS + " сек)");
    }

    // Обработка истёкших временных назначений
    private void processExpiredAssignments() {
        List<RoleAssignment> activeAssignments;
        synchronized (assignmentManager) {
            activeAssignments = assignmentManager.getActiveAssignments().stream()
                    .filter(ra -> ra instanceof TemporaryAssignment)
                    .collect(Collectors.toList());
        }

        int expiredCount = 0;
        for (RoleAssignment ra : activeAssignments) {
            if (ra instanceof TemporaryAssignment temp) {
                if (!temp.isActive()) {
                    synchronized (assignmentManager) {
                        if (temp.isActive()) continue;
                        assignmentManager.revokeAssignment(temp.assignmentId());
                    }
                    expiredCount++;
                    auditLog.log("ASSIGNMENT_EXPIRED", "scheduler", 
                        "TemporaryAssignment:" + temp.assignmentId(),
                        "Автоматически деактивировано");
                }
            }
        }
        if (expiredCount > 0) {
            System.out.println("[Scheduler] Деактивировано истёкших назначений: " + expiredCount);
        }
    }

    // Логирование статистики
    private void logPeriodicStatistics() {
        String stats = String.format("Users:%d Roles:%d Assignments:%d Active:%d",
            userManager.count(),
            roleManager.count(),
            assignmentManager.count(),
            assignmentManager.getActiveAssignments().size()
        );
        auditLog.log("PERIODIC_STATS", "scheduler", "RBACSystem", stats);
        System.out.println("[Scheduler] " + stats);
    }

    // ===== Статистика =====
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

    // ===== Корректное завершение =====
    public void shutdown() {
        System.out.println("Завершение работы системы...");

        // Остановка планировщика
        if (schedulerRunning) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            schedulerRunning = false;
            auditLog.log("SCHEDULER_STOPPED", "system", "RBACSystem", "Планировщик остановлен");
        }

        // Остановка остальных компонентов
        backgroundExecutor.shutdown();
        auditLog.shutdown();

        System.out.println("Система остановлена.");
    }
}