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

    // ====================== ПОЛЬЗОВАТЕЛИ ======================
    private static void registerUserCommands(CommandParser parser, RBACSystem system) {
        parser.registerCommand("user-list", "Список всех пользователей", (sc, sys) -> {
            sys.getUserManager().findAll().forEach(u -> System.out.println(u.format()));
        });

        parser.registerCommand("user-create", "Создать пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            String fullName = ConsoleUtils.promptString(sc, "Полное имя", true);
            String email = ConsoleUtils.promptString(sc, "Email", true);

            try {
                User user = User.create(username, fullName, email);
                sys.getUserManager().add(user);
                System.out.println("✅ Пользователь создан: " + user.format());
            } catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            sys.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
                System.out.println("Username: " + user.username());
                System.out.println("Full Name: " + user.fullName());
                System.out.println("Email: " + user.email());
                System.out.println("\nРоли:");
                sys.getAssignmentManager().findByUser(user)
                        .forEach(a -> System.out.println(" • " + a.role().getName()));
            }, () -> System.out.println("❌ Пользователь не найден."));
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            if (sys.getUserManager().findByUsername(username).isEmpty()) {
                System.out.println("❌ Пользователь не найден.");
                return;
            }
            if (ConsoleUtils.promptYesNo(sc, "Удалить пользователя " + username + "?")) {
                User user = sys.getUserManager().findByUsername(username).get();
                sys.getAssignmentManager().findByUser(user).forEach(sys.getAssignmentManager()::remove);
                sys.getUserManager().remove(user);
                System.out.println("✅ Пользователь удалён.");
            }
        });
    }

    // ====================== РОЛИ ======================
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

            if (!sys.getAssignmentManager().findByRole(role).isEmpty()) {
                System.out.println("⚠️ Роль назначена пользователям! Удаление запрещено.");
                return;
            }

            if (ConsoleUtils.promptYesNo(sc, "Удалить роль " + name + "?")) {
                sys.getRoleManager().remove(role);
                System.out.println("✅ Роль удалена.");
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (sc, sys) -> {
            String roleName = ConsoleUtils.promptString(sc, "Имя роли", true);
            String pName = ConsoleUtils.promptString(sc, "Название права", true);
            String pResource = ConsoleUtils.promptString(sc, "Ресурс", true);
            String pDesc = ConsoleUtils.promptString(sc, "Описание", false);

            Permission perm = new Permission(pName, pResource, pDesc);
            sys.getRoleManager().addPermissionToRole(roleName, perm);
            System.out.println("✅ Право добавлено.");
        });
    }

    // ====================== НАЗНАЧЕНИЯ ======================
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
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) return;

            var assigns = sys.getAssignmentManager().findByUser(userOpt.get())
                    .stream().filter(RoleAssignment::isActive).toList();

            if (assigns.isEmpty()) {
                System.out.println("Нет активных назначений.");
                return;
            }

            for (int i = 0; i < assigns.size(); i++) {
                System.out.println((i+1) + ". " + assigns.get(i).role().getName());
            }

            int idx = ConsoleUtils.promptInt(sc, "Номер для отзыва", 1, assigns.size()) - 1;
            sys.getAssignmentManager().revokeAssignment(assigns.get(idx).assignmentId());
            System.out.println("✅ Назначение отозвано.");
        });
    }

    // ====================== ПРОСМОТР ПРАВ ======================
    private static void registerPermissionCommands(CommandParser parser, RBACSystem system) {
        parser.registerCommand("permissions-user", "Все права пользователя", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            sys.getUserManager().findByUsername(username).ifPresentOrElse(user -> {
                Set<Permission> perms = sys.getAssignmentManager().getUserPermissions(user);
                perms.forEach(p -> System.out.println("• " + p.format()));
            }, () -> System.out.println("❌ Пользователь не найден."));
        });

        parser.registerCommand("permissions-check", "Проверить право", (sc, sys) -> {
            String username = ConsoleUtils.promptString(sc, "Username", true);
            String permName = ConsoleUtils.promptString(sc, "Название права", true);
            String resource = ConsoleUtils.promptString(sc, "Ресурс", true);

            boolean has = sys.getAssignmentManager().userHasPermission(
                    sys.getUserManager().findByUsername(username).orElse(null), permName, resource);

            System.out.println(has ? "✅ Доступ разрешён" : "❌ Доступ запрещён");
        });
    }

    // ====================== СЛУЖЕБНЫЕ ======================
    private static void registerUtilityCommands(CommandParser parser, RBACSystem system) {
        parser.registerCommand("help", "Справка", (sc, sys) -> parser.printHelp());
        parser.registerCommand("stats", "Статистика", (sc, sys) -> System.out.println(sys.generateStatistics()));
        parser.registerCommand("exit", "Выход", (sc, sys) -> {
            if (ConsoleUtils.promptYesNo(sc, "Выйти из программы?")) System.exit(0);
        });
    }
}