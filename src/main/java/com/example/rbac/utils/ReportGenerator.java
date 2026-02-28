package com.example.rbac.utils;

import com.example.rbac.managers.*;
import java.util.*;

public class ReportGenerator {

    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("Отчёт по пользователям:\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("%-20s %-30s %-30s %-15s%n", "Username", "Full Name", "Email", "Роли"));
        sb.append("------------------------------------------------------------\n");

        List<User> users = userManager.findAll();
        for (User user : users) {
            List<RoleAssignment> assigns = assignmentManager.findByUser(user);
            String roles = assigns.stream()
                    .filter(RoleAssignment::isActive)
                    .map(a -> a.role().getName())
                    .collect(Collectors.joining(", "));
            sb.append(String.format("%-20s %-30s %-30s %-15s%n",
                    user.username(), user.fullName(), user.email(), roles.isEmpty() ? "нет" : roles));
        }
        return sb.toString();
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("Отчёт по ролям:\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("%-20s %-40s %-10s%n", "Role Name", "Description", "Пользователей"));
        sb.append("------------------------------------------------------------\n");

        List<Role> roles = roleManager.findAll();
        for (Role role : roles) {
            long count = assignmentManager.findByRole(role).stream()
                    .filter(RoleAssignment::isActive)
                    .count();
            sb.append(String.format("%-20s %-40s %-10d%n",
                    role.getName(), role.getDescription(), count));
        }
        return sb.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("Матрица прав (пользователи × ресурсы):\n");
        // Здесь можно сделать более сложную таблицу, но для простоты — список
        sb.append("------------------------------------------------------------\n");
        for (User user : userManager.findAll()) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            sb.append(user.username() + ":\n");
            perms.forEach(p -> sb.append("  - " + p.format() + "\n"));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void exportToFile(String report, String filename) {
        // Пока заглушка — реализуем в 4.6 или отдельно
        System.out.println("Отчёт сохранён в " + filename + " (заглушка)");
        // Реальная реализация через Files.write ниже
    }
}
