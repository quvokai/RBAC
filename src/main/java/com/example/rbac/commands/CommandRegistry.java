package com.example.rbac.commands;

import com.example.rbac.*;
import com.example.rbac.filters.*;
import com.example.rbac.managers.*;
import com.example.rbac.system.RBACSystem;
import com.example.rbac.utils.ConsoleUtils;
import com.example.rbac.utils.ValidationUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAll(CommandParser parser, RBACSystem system) {
        registerUserCommands(parser, system);
        registerRoleCommands(parser, system);
        registerAssignmentCommands(parser, system);
        registerPermissionCommands(parser, system);
        registerUtilityCommands(parser, system);
    }

    // ==========================================
    // КОМАНДЫ ПОЛЬЗОВАТЕЛЕЙ
    // ==========================================
    private static void registerUserCommands(CommandParser parser, RBACSystem system) {

        parser.registerCommand("user-list", "Показать всех пользователей", (sc, sys) -> {
            List<User> users = sys.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("Пользователей пока нет.");
                return;
            }
            System.out.printf("%-15s | %-25s | %-30s%n", "Username", "Full Name", "Email");
            System.out.println("-".repeat(75));
            users.forEach(u -> System.out.printf("%-15s | %-25s | %-30s%n", u.username(), u.fullName(), u.email()));
        });

        parser.registerCommand("user-create", "Создать пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            String fullName = ConsoleUtils.promptString(sc, "Полное имя", true);
            String email = ConsoleUtils.promptString(sc, "Email", true);

            try {
                User user = User.create(username, fullName, email);
                sys.getUserManager().add(user);
                sys.getAuditLog().log("USER_CREATE", sys.getCurrentUser(), username, "Создан новый пользователь");
                System.out.println("✅ Пользователь успешно создан!");
            } catch (Exception e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотреть пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            sys.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
                System.out.println("=== Информация о пользователе ===");
                System.out.println("Username : " + user.username());
                System.out.println("Full Name: " + user.fullName());
                System.out.println("Email    : " + user.email());

                System.out.println("\n--- Назначенные роли ---");
                sys.getAssignmentManager().findByUser(user).forEach(a ->
                    System.out.println(" • " + a.role().getName() + " [" + (a.isActive() ? "активна" : "неактивна") + "]")
                );
            }, () -> System.out.println("❌ Пользователь не найден."));
        });

        parser.registerCommand("user-update", "Обновить пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            if (!sys.getUserManager().exists(username)) {
                System.out.println("❌ Пользователь не найден.");
                return;
            }
            String newFullName = ConsoleUtils.promptString(sc, "Новое полное имя", true);
            String newEmail = ConsoleUtils.promptString(sc, "Новый Email", true);

            try {
                sys.getUserManager().update(username, newFullName, newEmail);
                System.out.println("✅ Данные обновлены.");
            } catch (Exception e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            if (!sys.getUserManager().exists(username)) {
                System.out.println("❌ Пользователь не найден.");
                return;
            }
            if (ConsoleUtils.promptYesNo(sc, "Вы уверены, что хотите удалить пользователя " + username + "?")) {
                User user = sys.getUserManager().findByUsername(username).get();
                sys.getAssignmentManager().findByUser(user).forEach(sys.getAssignmentManager()::remove);
                sys.getUserManager().remove(user);
                System.out.println("✅ Пользователь и его назначения удалены.");
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей", (sc, sys) -> {
            System.out.println("1. По username\n2. По email\n3. По домену\n4. По имени");
            String choice = ConsoleUtils.promptString(sc, "Выберите фильтр", true);

            String query = ConsoleUtils.promptString(sc, "Строка поиска", true).toLowerCase();

            List<User> result = sys.getUserManager().findAll().stream().filter(u -> {
                switch (choice) {
                    case "1": return u.username().toLowerCase().contains(query);
                    case "2": return u.email().toLowerCase().contains(query);
                    case "3": return u.email().toLowerCase().endsWith(query);
                    case "4": return u.fullName().toLowerCase().contains(query);
                    default: return false;
                }
            }).toList();

            if (result.isEmpty()) System.out.println("Ничего не найдено.");
            else result.forEach(u -> System.out.println("• " + u.username() + " (" + u.email() + ")"));
        });
    }

    // ==========================================
    // КОМАНДЫ РОЛЕЙ
    // ==========================================
    private static void registerRoleCommands(CommandParser parser, RBACSystem system) {
        parser.registerCommand("role-list", "Список ролей", (sc, sys) -> {
            sys.getRoleManager().findAll().forEach(r -> System.out.println(r.format()));
        });

        parser.registerCommand("role-create", "Создать роль", (sc, sys) -> {
            String name = ConsoleUtils.promptString(sc, "Название роли", true);
            String desc = ConsoleUtils.promptString(sc, "Описание", false);

            Role role = new Role(name, desc);
            sys.getRoleManager().add(role);
            System.out.println("✅ Роль создана: " + name);
        });

        parser.registerCommand("role-delete", "Удалить роль", (sc, sys) -> {
            String name = ConsoleUtils.promptString(sc, "Имя роли", true);
            Optional<Role> roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("❌ Роль не найдена.");
                return;
            }
            Role role = roleOpt.get();

            List<RoleAssignment> assignments = sys.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("⚠️ Роль назначена пользователям! Удаление невозможно.");
                return;
            }

            if (ConsoleUtils.promptYesNo(sc, "Удалить роль " + name + "?")) {
                sys.getRoleManager().remove(role);
                System.out.println("✅ Роль удалена.");
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (sc, sys) -> {
            String roleName = ConsoleUtils.promptString(sc, "Имя роли", true);
            String pName = ConsoleUtils.promptString(sc, "Название права (READ/WRITE...)", true);
            String pResource = ConsoleUtils.promptString(sc, "Ресурс", true);
            String pDesc = ConsoleUtils.promptString(sc, "Описание", false);

            Permission perm = new Permission(pName, pResource, pDesc);
            sys.getRoleManager().addPermissionToRole(roleName, perm);
            System.out.println("✅ Право добавлено.");
        });
    }

    // ==========================================
    // КОМАНДЫ НАЗНАЧЕНИЙ
    // ==========================================
    private static void registerAssignmentCommands(CommandParser parser, RBACSystem system) {
        parser.registerCommand("assign-role", "Назначить роль", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            String roleName = ConsoleUtils.promptString(sc, "Имя роли", true);

            var userOpt = sys.getUserManager().findByUsername(username);
            var roleOpt = sys.getRoleManager().findByName(roleName);

            if (userOpt.isEmpty() || roleOpt.isEmpty()) {
                System.out.println("❌ Пользователь или роль не найдены.");
                return;
            }

            AssignmentMetadata meta = AssignmentMetadata.now(sys.getCurrentUser(), "Через консоль");
            PermanentAssignment assignment = new PermanentAssignment(userOpt.get(), roleOpt.get(), meta);

            try {
                sys.getAssignmentManager().add(assignment);
                System.out.println("✅ Роль назначена.");
            } catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            var assignments = sys.getAssignmentManager().findByUser(
                    sys.getUserManager().findByUsername(username).orElse(null));

            if (assignments.isEmpty()) {
                System.out.println("Нет активных назначений.");
                return;
            }

            System.out.println("Активные назначения:");
            for (int i = 0; i < assignments.size(); i++) {
                System.out.println((i+1) + ". " + assignments.get(i).role().getName());
            }

            int choice = ConsoleUtils.promptInt(sc, "Номер для отзыва", 1, assignments.size());
            sys.getAssignmentManager().revokeAssignment(assignments.get(choice-1).assignmentId());
            System.out.println("✅ Назначение отозвано.");
        });
    }

    // ==========================================
    // СЛУЖЕБНЫЕ КОМАНДЫ
    // ==========================================
    private static void registerUtilityCommands(CommandParser parser, RBACSystem system) {
        parser.registerCommand("help", "Справка", (sc, sys) -> parser.printHelp());
        parser.registerCommand("stats", "Статистика", (sc, sys) -> System.out.println(sys.generateStatistics()));
        parser.registerCommand("exit", "Выход", (sc, sys) -> {
            if (ConsoleUtils.promptYesNo(sc, "Выйти из программы?")) {
                System.out.println("До свидания!");
                System.exit(0);
            }
        });
    }
}