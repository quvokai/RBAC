package com.example.rbac.commands;

import com.example.rbac.*;
import com.example.rbac.filters.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAll(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerUtilityCommands(parser);
    }

    private static void registerUserCommands(CommandParser parser) {
        
        parser.registerCommand("user-list", "Вывести список всех пользователей", (scanner, system) -> {
            System.out.print("Применить фильтр поиска? (да/нет): ");
            boolean applyFilter = scanner.nextLine().trim().equalsIgnoreCase("да");
            
            List<User> users;
            if (applyFilter) {
                System.out.print("Введите строку для фильтрации по имени/email: ");
                String query = scanner.nextLine().toLowerCase();
                users = system.getUserManager().findAll().stream()
                        .filter(u -> u.username().toLowerCase().contains(query) || u.email().toLowerCase().contains(query))
                        .collect(Collectors.toList());
            } else {
                users = system.getUserManager().findAll();
            }

            System.out.printf("%-15s | %-25s | %-25s%n", "Username", "Full Name", "Email");
            System.out.println("-".repeat(70));
            users.forEach(u -> System.out.printf("%-15s | %-25s | %-25s%n", u.username(), u.fullName(), u.email()));
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine().trim();
            System.out.print("Full Name: "); String fn = scanner.nextLine().trim();
            System.out.print("Email: "); String em = scanner.nextLine().trim();

            if (un.isEmpty() || em.isEmpty() || !em.contains("@")) {
                System.out.println("Ошибка: Некорректные данные. Username обязателен, Email должен содержать '@'.");
                return;
            }
            try {
                system.getUserManager().add(User.create(un, fn, em));
                System.out.println("Успех: Пользователь " + un + " создан.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.print("Введите username: "); String un = scanner.nextLine();
            system.getUserManager().findByUsername(un).ifPresentOrElse(u -> {
                System.out.println("=== Информация о пользователе ===");
                System.out.println("Username:  " + u.username());
                System.out.println("Full Name: " + u.fullName());
                System.out.println("Email:     " + u.email());
                
                System.out.println("\n--- Назначенные роли ---");
                system.getAssignmentManager().findByUser(u).forEach(a -> 
                    System.out.println("- " + a.role().getName() + " [" + (a.isActive() ? "Активна" : "Неактивна") + "]")
                );

                System.out.println("\n--- Все права ---");
                system.getAssignmentManager().getUserPermissions(u).forEach(p -> 
                    System.out.println("- " + p.resource() + " : " + p.name())
                );
            }, () -> System.out.println("Пользователь не найден."));
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.print("Введите username: "); String un = scanner.nextLine();
            if (!system.getUserManager().exists(un)) {
                System.out.println("Пользователь не найден."); return;
            }
            System.out.print("Новое Full Name: "); String fn = scanner.nextLine();
            System.out.print("Новый Email: "); String em = scanner.nextLine();
            
            try {
                system.getUserManager().update(un, fn, em);
                System.out.println("Данные пользователя обновлены.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("Введите username: "); String un = scanner.nextLine();
            Optional<User> userOpt = system.getUserManager().findByUsername(un);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден."); return;
            }
            System.out.print("Вы уверены, что хотите удалить пользователя " + un + "? (да/нет): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("да")) {
                User u = userOpt.get();
                system.getAssignmentManager().findByUser(u).forEach(system.getAssignmentManager()::remove);
                system.getUserManager().remove(u);
                System.out.println("Пользователь и его назначения успешно удалены.");
            } else {
                System.out.println("Удаление отменено.");
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            System.out.println("Выберите фильтр:\n1. По username\n2. По email\n3. По домену email\n4. По полному имени");
            System.out.print("Выбор: "); String choice = scanner.nextLine();
            System.out.print("Введите значение для поиска: "); String query = scanner.nextLine().toLowerCase();

            List<User> results = system.getUserManager().findAll().stream().filter(u -> {
                switch (choice) {
                    case "1": return u.username().toLowerCase().contains(query);
                    case "2": return u.email().toLowerCase().contains(query);
                    case "3": return u.email().toLowerCase().endsWith(query);
                    case "4": return u.fullName().toLowerCase().contains(query);
                    default: return false;
                }
            }).collect(Collectors.toList());

            if (results.isEmpty()) System.out.println("Ничего не найдено.");
            else results.forEach(u -> System.out.println("- " + u.username() + " (" + u.email() + ")"));
        });
    }

    private static void registerRoleCommands(CommandParser parser) {

        parser.registerCommand("role-list", "Вывести список всех ролей", (scanner, system) -> {
            System.out.printf("%-10s | %-20s | %-10s%n", "ID", "Название", "Прав");
            System.out.println("-".repeat(45));
            system.getRoleManager().findAll().forEach(r -> 
                System.out.printf("%-10s | %-20s | %-10d%n", r.getId(), r.getName(), r.getPermissions().size())
            );
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.print("Название роли: "); String name = scanner.nextLine();
            System.out.print("Описание: "); String desc = scanner.nextLine();
            String id = UUID.randomUUID().toString().substring(0, 8);
            
            Role newRole = new Role(name, desc);
            try {
                system.getRoleManager().add(newRole);
                System.out.println("Роль создана.");
                
                while (true) {
                    System.out.print("Добавить право? (да/нет): ");
                    if (!scanner.nextLine().trim().equalsIgnoreCase("да")) break;
                    
                    System.out.print("Действие (напр. READ): "); String pName = scanner.nextLine();
                    System.out.print("Ресурс (напр. USERS): "); String pRes = scanner.nextLine();
                    System.out.print("Описание права: "); String pDesc = scanner.nextLine();
                    
                    system.getRoleManager().addPermissionToRole(name, new Permission(pName, pRes, pDesc));
                    System.out.println("Право добавлено.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            System.out.print("Введите имя роли: "); String name = scanner.nextLine();
            system.getRoleManager().findByName(name).ifPresentOrElse(r -> {
                // Если в классе Role есть метод format(), используй его: System.out.println(r.format());
                System.out.println("ID: " + r.getId() + "\nИмя: " + r.getName() + "\nОписание: " + r.getDescription());
                System.out.println("Права:");
                r.getPermissions().forEach(p -> System.out.println(" - " + p.name() + " on " + p.resource()));
            }, () -> System.out.println("Роль не найдена."));
        });

        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
    System.out.print("Введите имя роли для обновления: "); String name = scanner.nextLine();
    Optional<Role> roleOpt = system.getRoleManager().findByName(name);
    if (roleOpt.isEmpty()) {
        System.out.println("Роль не найдена."); return;
    }
    
    System.out.print("Новое название: "); String newName = scanner.nextLine();
    System.out.print("Новое описание: "); String newDesc = scanner.nextLine();
    
    Role oldRole = roleOpt.get();

    Role updatedRole = new Role(
        newName.isEmpty() ? oldRole.getName() : newName,
        newDesc.isEmpty() ? oldRole.getDescription() : newDesc
    );
    

    oldRole.getPermissions().forEach(updatedRole::addPermission);
    
    system.getRoleManager().remove(oldRole);
    system.getRoleManager().add(updatedRole);
    System.out.println("Роль обновлена.");
});

       parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
    System.out.print("Введите имя роли: "); 
    // Используем trim(), чтобы пробелы или пустые переносы не ломали поиск
    String name = scanner.nextLine().trim(); 
    
    Optional<Role> roleOpt = system.getRoleManager().findByName(name);
    if (roleOpt.isEmpty()) {
        System.out.println("Роль не найдена.");
        return;
    }
    
    Role role = roleOpt.get();
    List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
    
    if (!assignments.isEmpty()) {
        System.out.println("ВНИМАНИЕ! Эта роль назначена следующим пользователям:");
        assignments.forEach(a -> System.out.println("- " + a.user().username()));
    }
    
    System.out.print("Вы уверены, что хотите удалить роль? (да/нет): ");
    String confirmation = scanner.nextLine().trim();
    
    if (confirmation.equalsIgnoreCase("да")) {
        
        assignments.forEach(system.getAssignmentManager()::remove);
      
        system.getRoleManager().remove(role);
        System.out.println("Роль и все её назначения удалены.");
    } else {
        System.out.println("Удаление отменено.");
    }
});

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            System.out.print("Действие (name): "); String pName = scanner.nextLine();
            System.out.print("Ресурс (resource): "); String pRes = scanner.nextLine();
            System.out.print("Описание: "); String pDesc = scanner.nextLine();
            
            try {
                system.getRoleManager().addPermissionToRole(rName, new Permission(pName, pRes, pDesc));
                System.out.println("Право добавлено.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            Optional<Role> roleOpt = system.getRoleManager().findByName(rName);
            if (roleOpt.isEmpty()) { System.out.println("Роль не найдена."); return; }
            
            Role role = roleOpt.get();
            List<Permission> perms = new ArrayList<>(role.getPermissions());
            if (perms.isEmpty()) { System.out.println("У роли нет прав."); return; }
            
            for (int i = 0; i < perms.size(); i++) {
                System.out.printf("%d. [%s] %s%n", i + 1, perms.get(i).resource(), perms.get(i).name());
            }
            
            System.out.print("Введите номер права для удаления: ");
            try {
                int index = Integer.parseInt(scanner.nextLine()) - 1;
                system.getRoleManager().removePermissionFromRole(rName, perms.get(index));
                System.out.println("Право удалено.");
            } catch (Exception e) {
                System.out.println("Неверный ввод.");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей", (scanner, system) -> {
            System.out.println("1. По имени (содержит)\n2. По наличию ресурса/права\n3. По минимальному количеству прав");
            System.out.print("Выбор: "); String choice = scanner.nextLine();
            
            List<Role> roles = system.getRoleManager().findAll();
            if (choice.equals("1")) {
                System.out.print("Строка поиска: "); String q = scanner.nextLine().toLowerCase();
                roles.stream().filter(r -> r.getName().toLowerCase().contains(q)).forEach(r -> System.out.println(r.getName()));
            } else if (choice.equals("2")) {
                System.out.print("Имя ресурса/права: "); String q = scanner.nextLine();
                roles.stream().filter(r -> r.getPermissions().stream().anyMatch(p -> p.name().contains(q) || p.resource().contains(q)))
                     .forEach(r -> System.out.println(r.getName()));
            } else if (choice.equals("3")) {
                System.out.print("Минимум прав: "); int min = Integer.parseInt(scanner.nextLine());
                roles.stream().filter(r -> r.getPermissions().size() >= min).forEach(r -> System.out.println(r.getName()));
            }
        });
    }


    private static void registerAssignmentCommands(CommandParser parser) {

    parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {

    System.out.print("Username: "); 
    String un = scanner.nextLine().trim();
    
    System.out.println("Доступные роли:");
    system.getRoleManager().findAll().forEach(r -> System.out.println("- " + r.getName()));
    
    System.out.print("Выбор роли: "); 
    String rn = scanner.nextLine().trim();
    

    var userOpt = system.getUserManager().findByUsername(un);
    var roleOpt = system.getRoleManager().findByName(rn);
    
    if (userOpt.isEmpty() || roleOpt.isEmpty()) {
        System.out.println("Ошибка: Пользователь или роль не найдены.");
        return;
    }
    

    System.out.print("Тип (1 - Постоянное, 2 - Временное): "); 
    String typeChoice = scanner.nextLine().trim();
    
    System.out.print("Причина назначения: "); 
    String reason = scanner.nextLine().trim();

    AssignmentMetadata meta = AssignmentMetadata.now("admin", reason);
    
    try {
        if (typeChoice.equals("1")) {

            PermanentAssignment pa = new PermanentAssignment(userOpt.get(), roleOpt.get(), meta);
            system.getAssignmentManager().add(pa);
        } else {
            System.out.print("Дата истечения (формат ГГГГ-ММ-ДД чч:мм): "); 
            String expiryDate = scanner.nextLine().trim();

            TemporaryAssignment ta = new TemporaryAssignment(userOpt.get(), roleOpt.get(), meta, expiryDate, false);
            system.getAssignmentManager().add(ta);
        }
        System.out.println("Успех: Роль успешно назначена.");
    } catch (Exception e) {

        System.out.println("Ошибка при назначении: " + e.getMessage());
    }
});

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            var userOpt = system.getUserManager().findByUsername(un);
            if (userOpt.isEmpty()) return;
            
            List<RoleAssignment> active = system.getAssignmentManager().findByUser(userOpt.get()).stream()
                    .filter(RoleAssignment::isActive).collect(Collectors.toList());
            
            if (active.isEmpty()) { System.out.println("Нет активных назначений."); return; }
            
            for (int i = 0; i < active.size(); i++) {
                System.out.printf("%d. [%s] %s%n", i + 1, active.get(i).assignmentId(), active.get(i).role().getName());
            }
            
            System.out.print("Выберите номер для отзыва: ");
            try {
                int idx = Integer.parseInt(scanner.nextLine()) - 1;
                system.getAssignmentManager().revokeAssignment(active.get(idx).assignmentId());
                System.out.println("Назначение отозвано (помечено неактивным).");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            System.out.printf("%-15s | %-15s | %-10s | %-10s%n", "User", "Role", "Type", "Status");
            System.out.println("-".repeat(60));
            system.getAssignmentManager().findAll().forEach(a -> {
                String type = a instanceof PermanentAssignment ? "Permanent" : "Temporary";
                String status = a.isActive() ? "Active" : "Inactive";
                System.out.printf("%-15s | %-15s | %-10s | %-10s%n", a.user().username(), a.role().getName(), type, status);
            });
        });

        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            system.getUserManager().findByUsername(un).ifPresentOrElse(u -> {
                system.getAssignmentManager().findByUser(u).forEach(a -> 
                    System.out.println(a.assignmentId() + " | " + a.role().getName() + " | Активно: " + a.isActive()));
            }, () -> System.out.println("Пользователь не найден."));
        });

        parser.registerCommand("assignment-list-role", "Список пользователей с конкретной ролью", (scanner, system) -> {
            System.out.print("Имя роли: "); String rName = scanner.nextLine();
            system.getRoleManager().findByName(rName).ifPresentOrElse(r -> {
                system.getAssignmentManager().findByRole(r).stream()
                      .map(a -> a.user().username()).distinct()
                      .forEach(System.out::println);
            }, () -> System.out.println("Роль не найдена."));
        });

        parser.registerCommand("assignment-active", "Только активные назначения", (scanner, system) -> {
            system.getAssignmentManager().getActiveAssignments().forEach(a -> 
                System.out.println(a.user().username() + " -> " + a.role().getName()));
        });

        parser.registerCommand("assignment-expired", "Истёкшие временные назначения", (scanner, system) -> {
            system.getAssignmentManager().getExpiredAssignments().forEach(a -> 
                System.out.println(a.assignmentId() + " | " + a.user().username() + " -> " + a.role().getName()));
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            System.out.print("Введите Assignment ID: "); String aId = scanner.nextLine();
            System.out.print("Новая дата истечения (YYYY-MM-DD): "); String newDate = scanner.nextLine();
            try {
                system.getAssignmentManager().extendTemporaryAssignment(aId, newDate);
                System.out.println("Назначение продлено.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам", (scanner, system) -> {
            System.out.println("1. По пользователю\n2. По статусу (активное)");
            System.out.print("Выбор: "); String choice = scanner.nextLine();
            
            List<RoleAssignment> list = system.getAssignmentManager().findAll();
            if (choice.equals("1")) {
                System.out.print("Username: "); String un = scanner.nextLine();
                list.stream().filter(a -> a.user().username().equalsIgnoreCase(un)).forEach(a -> System.out.println(a.assignmentId()));
            } else if (choice.equals("2")) {
                list.stream().filter(RoleAssignment::isActive).forEach(a -> System.out.println(a.assignmentId() + " - " + a.user().username()));
            }
        });
    }

    // ==========================================
    // ПРОСМОТР ПРАВ
    // ==========================================
    private static void registerPermissionCommands(CommandParser parser) {
        
        parser.registerCommand("permissions-user", "Все права пользователя", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            system.getUserManager().findByUsername(un).ifPresentOrElse(u -> {
                Set<Permission> perms = system.getAssignmentManager().getUserPermissions(u);
                
                // Группировка по ресурсам
                Map<String, List<Permission>> grouped = perms.stream()
                        .collect(Collectors.groupingBy(Permission::resource));
                
                grouped.forEach((resource, pList) -> {
                    System.out.println("\nРесурс: [" + resource + "]");
                    pList.forEach(p -> System.out.println(" - " + p.name()));
                });
            }, () -> System.out.println("Пользователь не найден."));
        });

        parser.registerCommand("permissions-check", "Проверить наличие права", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            System.out.print("Право (name): "); String pName = scanner.nextLine();
            System.out.print("Ресурс (resource): "); String pRes = scanner.nextLine();
            
            system.getUserManager().findByUsername(un).ifPresentOrElse(u -> {
                boolean hasPerm = system.getAssignmentManager().userHasPermission(u, pName, pRes);
                System.out.println("Результат: " + (hasPerm ? "ДОСТУП РАЗРЕШЕН" : "В ДОСТУПЕ ОТКАЗАНО"));
            }, () -> System.out.println("Пользователь не найден."));
        });
    }

    // ==========================================
    // СЛУЖЕБНЫЕ КОМАНДЫ
    // ==========================================
    private static void registerUtilityCommands(CommandParser parser) {
        
        parser.registerCommand("help", "Справка по командам", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            for (int i = 0; i < 20; i++) System.out.println(); // Fallback
        });

        parser.registerCommand("save", "Сохранить данные", (scanner, system) -> {
            System.out.println("Сохранение данных в JSON (Заглушка. Здесь реализуется логика сериализации).");
            // File I/O logic goes here
            System.out.println("Данные успешно сохранены.");
        });

        parser.registerCommand("load", "Загрузить данные", (scanner, system) -> {
            System.out.println("Чтение данных из файла (Заглушка. Здесь реализуется логика десериализации).");
            // File I/O logic goes here
            System.out.println("Данные загружены.");
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            System.out.print("Желаете сохранить данные перед выходом? (да/нет): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("да")) {
                System.out.println("Сохранение..."); // Вызов логики save
            }
            System.out.print("Точно выйти? (да/нет): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("да")) {
                System.out.println("Завершение работы системы. До свидания!");
                System.exit(0);
            }
        });
    }
}